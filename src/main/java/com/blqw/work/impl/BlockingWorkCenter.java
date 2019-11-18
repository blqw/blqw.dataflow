package com.blqw.work.impl;

import com.blqw.dataflow.core.*;
import com.blqw.dataflow.define.*;
import com.blqw.work.core.*;
import com.blqw.work.define.*;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @param <TIn>
 * @param <TOut>
 */
public class BlockingWorkCenter<TIn, TOut> implements IWorkCenter<TIn, TOut>, IWorkHandler<TIn, TOut> {

    public static final String BUFFER_SIZE = "BUFFER_SIZE";
    public static final String INTERVAL_SECONDS = "INTERVAL_SECONDS";
    public static final String MAX_ERROR_TIMES = "MAX_ERROR_TIMES";


    private final DefaultWorkHandler<TIn, TOut> handler = new DefaultWorkHandler(this);

    private final List<Runnable> destroyable = new ArrayList<>();
    private final WorkStatusMachine status = new WorkStatusMachine(handler);
    private final AtomicInteger errorTimes = new AtomicInteger(0);

    private BlockingQueue<Object> dataQueue;
    private ExecutorService readExecutor;
    private ExecutorService processExecutor;
    private CountDownLatch readCounter;
    private CountDownLatch processCounter;
    private ScheduledExecutorService snapshotTimer;

    private WorkSettings<TIn, TOut> settings;

    private Exception exception;
    private String[] cursors;

    public final String ticket;
    private int bufferSize;
    private int maxErrorTimes;
    private int intervalSeconds;

    public BlockingWorkCenter() {
        this((WorkSettings<TIn, TOut>) null);
    }

    public BlockingWorkCenter(WorkSettings<TIn, TOut> settings) {
        this(UUID.randomUUID().toString().replace("-", "").toUpperCase(), settings);
    }

    public BlockingWorkCenter(String ticket) {
        this(ticket, null);
    }

    public BlockingWorkCenter(String ticket, WorkSettings<TIn, TOut> settings) {
        this.ticket = ticket;
        if (settings != null) {
            configuration(settings);
        }
    }

    private boolean isReady() {
        return settings != null
                && settings.getReaders() != null
                && settings.getReaders().length > 0
                && settings.getProcesses() != null
                && settings.getProcesses().length > 0;
    }

    @Override
    public void configuration(WorkSettings<TIn, TOut> settings) {
        if (status.change(WorkStatus.preparation, () -> this.settings = settings.cloneSettings())) {
            if (isReady()) {
                status.change(WorkStatus.ready, null);
            }
        } else {
            throw new WorkException("当前状态不允许该操作");
        }
    }

    @Override
    public void setHandler(IWorkHandler<TIn, TOut> handler) {
        this.handler.add(handler);
    }


    @Override
    public void startAsync(int readThreads, int processThreads) {
        String[] cursors = loadCursors();
        if (!status.change(WorkStatus.running, () -> prestart(readThreads, processThreads))) {
            switch (status.get()) {
                case preparation:
                    throw new WorkException("未就绪");
                case ready:
                    throw new WorkException("系统内部错误");
                case running:
                    throw new WorkException("正在运行中");
                case completed:
                    throw new WorkException("已经完成");
                case destroyed:
                    throw new WorkException("被销毁");
                case canceled:
                    throw new WorkException("因出现错误或执行取消操作而中断运行");
                default:
                    throw new WorkCanceledException("未知的状态: " + status.get());
            }
        }
        IDataReader<TIn>[] readers = settings.getReaders();
        IDataProcess<TOut>[] processes = settings.getProcesses();
        this.cursors = cursors;
        for (int i = 0; i < readers.length; i++) {
            readExecutor.execute(createReadRunnable(i, readers[i]));
        }
        int length = Math.max(processThreads, 1);
        for (int i = 0; i < length; ) {
            for (IDataProcess<TOut> process : processes) {
                processExecutor.execute(createProcessRunnable(i, process));
                i++;
            }
        }
    }

    private void runningCheck(String errorMessage) {
        if (status.get() != WorkStatus.running) {
            println("status=" + status.get().desc + ", exit");
            throw new WorkCanceledException(errorMessage + "原因:状态=" + status.get().desc);
        }
    }

    private Runnable createProcessRunnable(int index, IDataProcess<TOut> process) {
        handler.onBeforeProcess(index, process);
        return new Runnable() {
            final IDataKeyFetcher<TOut> keyfetcher = settings.getKeyfetcher();
            final IStringCache cache = settings.getCache();
            final long timeout = System.currentTimeMillis() + intervalSeconds * 1000;
            DataCursor cursor;
            boolean isEnd;
            boolean first = true;
            final String errorMessage = "数据输出[" + index + "]终止;";

            private void addCache(TOut data) {
                if (keyfetcher != null) {
                    cache.set(ticket + "=" + keyfetcher.getKey(data), "1", 86400);
                }
            }

            private boolean hasCache(TOut data) {
                return keyfetcher != null && cache.has(ticket + "=" + keyfetcher.getKey(data));
            }

            private void saveCursor() {
                if (cursor != null) {
                    cursors[cursor.index] = cursor.cursor;
                }
            }

            private boolean isTimeout() {
                return timeout < System.currentTimeMillis();
            }

            @Override
            public void run() {
                if (first) {
                    println("process[" + index + "] run.");
                    first = false;
                }
                do {
                    TOut data = null;
                    try {
                        Object obj = dataQueue.poll(5, TimeUnit.SECONDS);
                        runningCheck(errorMessage);
                        if (obj != null) {
                            if (obj instanceof DataCursor) {
                                DataCursor cursor = (DataCursor) obj;
                                this.cursor = cursor;
                                data = (TOut) cursor.data;
                            } else {
                                data = (TOut) obj;
                            }
                            if (hasCache(data)) {
                                println("skip KEY=" + keyfetcher.getKey(data));
                            } else {
                                handler.onProcessing(index, process, data);
                                process.set(data);
                                addCache(data);
                                handler.onAfterProcess(index, process, data);
                                saveCursor();
                            }
                        }

                        isEnd = dataQueue.size() == 0 && readCounter.getCount() == 0;
                    } catch (Exception e) {
                        isEnd = e instanceof WorkCanceledException;
                        handler.onError(e);
                    } finally {
                        cursor = null;
                        if (isEnd) {
                            processCounter.countDown();
                            println("process[" + index + "] exit.");
                            handler.onProcessEnd(index, process);
                            if (processCounter.getCount() == 0) {
                                destroy(WorkStatus.completed);
                            }
                        }
                    }
                } while (!isEnd && !isTimeout());

                if (!isEnd) {
                    processExecutor.execute(this);
                }
            }
        };
    }

    private Runnable createReadRunnable(int infolwIndex, IDataReader<TIn> reader) {
        handler.onBeforeRead(infolwIndex, reader);

        return new Runnable() {
            final int index = infolwIndex;
            final IDataTransform<TIn, TOut> dataTransform = settings.getDataTransform();
            String cursor = Optional.ofNullable(cursors[index]).orElse("");
            boolean isEnd = false;
            final String errorMessage = "数据输入[" + index + "]终止;";
            boolean first = true;

            private TOut transform(TIn data) {
                if (dataTransform == null) {
                    return (TOut) data;
                }
                return dataTransform.to(data);
            }

            @Override
            public void run() {
                if (first) {
                    println("reader[" + index + "] run.");
                    first = false;
                }
                try {
                    runningCheck(errorMessage);
                    handler.onReading(index, reader, cursor);
                    IBatchData<TIn> batch = reader.get(cursor, null);
                    isEnd = batch.isEnd();
                    List<TIn> list = batch.data();
                    if (list.size() > 0) {
                        for (int i = 0; i < list.size() - 1; i++) {
                            dataQueue.put(transform(list.get(i)));
                        }
                        dataQueue.put(new DataCursor(index, cursor, transform(list.get(list.size() - 1)))); // 保存当前进度
                    }
                    cursor = batch.cursor();
                    handler.onAfterRead(index, reader, batch);
                } catch (Exception e) {
                    isEnd = e instanceof WorkCanceledException;
                    handler.onError(e, index, cursor);
                } finally {
                    if (isEnd) {
                        readCounter.countDown();
                        println("reader[" + index + "] exit.");
                        handler.onReadEnd(index, reader);
                    } else {
                        readExecutor.execute(this);
                    }
                }
            }
        };
    }

    private void prestart(int inputThreads, int outputThreads) {

        // properties
        intervalSeconds = (int) settings.getProperty(INTERVAL_SECONDS, 30);
        maxErrorTimes = (int) settings.getProperty(MAX_ERROR_TIMES, 100);
        bufferSize = (int) settings.getProperty(BUFFER_SIZE, 1000);

        // cache
        IStringCache cache = settings.getCache();
        if (cache == null) {
            settings.setCache(cache = new MemoryCache());
        }
        destroyable.add(cache::release);

        // dataQueue
        dataQueue = new ArrayBlockingQueue<>(bufferSize);
        destroyable.add(dataQueue::clear);

        // readers
        IDataReader<TIn>[] readers = settings.getReaders();
        readExecutor = Executors.newWorkStealingPool(inputThreads <= 0 ? readers.length : inputThreads);
        readCounter = new CountDownLatch(readers.length);
        destroyable.add(readExecutor::shutdown);

        // processes
        IDataProcess<TOut>[] processes = settings.getProcesses();
        if (outputThreads <= 0 || outputThreads % processes.length == 0) {
            processExecutor = Executors.newWorkStealingPool(Math.max(processes.length, outputThreads));
            intervalSeconds = 3600;
        } else {
            processExecutor = Executors.newWorkStealingPool(outputThreads);
        }
        processCounter = new CountDownLatch((int) Math.ceil((double) outputThreads / (double) processes.length) * processes.length);
        destroyable.add(processExecutor::shutdown);

        IWorkSnapshoter snapshoter = settings.getSnapshoter();
        if (snapshoter != null) {
            snapshotTimer = Executors.newScheduledThreadPool(1);
            snapshotTimer.scheduleAtFixedRate(() -> {
                println("保存快照: " + Arrays.toString(cursors));
                try {
                    snapshoter.save(cursors);
                } catch (Exception e) {
                    handler.onError(e, cursors);
                }
            }, 10, 10, TimeUnit.SECONDS);
            destroyable.add(() -> {
                snapshotTimer.shutdown();
                println("保存快照: " + Arrays.toString(cursors));
                snapshoter.save(cursors);
            });
        }
    }

    private String[] loadCursors() {
        IWorkSnapshoter snapshoter = settings.getSnapshoter();
        IDataReader<TIn>[] readers = settings.getReaders();
        if (snapshoter == null) {
            return new String[readers.length];
        }

        String[] cursors = snapshoter.load();
        println("读取快照: " + Arrays.toString(cursors));
        if (cursors == null) {
            cursors = new String[readers.length];
        } else if (cursors.length != readers.length) {
            handler.onError(new WorkException("快照不匹配当前操作, 重置进度"), cursors, readers.length);
            cursors = new String[readers.length];
        }

        return cursors;
    }

    static final DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private void println(String message) {
        System.out.println(MessageFormat.format("{0}  com.blqw.work.impl.BlockingWorkCenter[{1}] >> {2}"
                , dateformatter.format(LocalDateTime.now())
                , ticket
                , message)
        );
    }

    @Override
    public WorkStatus getStatus() {
        return status.get();
    }

    @Override
    public Exception getLastException() {
        return exception;
    }

    @Override
    public void destroy() {
        destroy(WorkStatus.destroyed);
    }


    public void destroy(WorkStatus reason) {
        if (!status.change(reason, null)) {
            return;
        }
        destroyable.forEach(x -> {
            try {
                x.run();
            } catch (Exception e) {
                handler.onError(e, reason);
            }
        });
        destroyable.clear();
        onDestroyed();
    }

    @Override
    public void onStatusChanged(WorkStatus current, WorkStatus old) {
        println("状态变更:" + old + " -> " + current);
    }

    @Override
    public void onDestroyed() {
        println("退出...");
    }

    @Override
    public void onError(Exception exception, Object... args) {
        if (exception == null) {
            return;
        }
        this.exception = exception;
        println(Arrays.toString(args));
        exception.printStackTrace();
        if (exception instanceof WorkCanceledException || errorTimes.incrementAndGet() > maxErrorTimes) {
            destroy(WorkStatus.canceled);
        }
    }

    @Override
    public void await() {
        if (processCounter != null) {
            try {
                processCounter.await();
            } catch (InterruptedException e) {
                handler.onError(e);
            }
        }
    }
}