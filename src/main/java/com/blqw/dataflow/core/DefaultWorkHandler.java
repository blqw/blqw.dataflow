package com.blqw.dataflow.core;

import com.blqw.dataflow.define.IBatchData;
import com.blqw.work.core.WorkStatus;
import com.blqw.work.define.IWorkHandler;
import com.blqw.work.define.IDataReader;
import com.blqw.work.define.IDataProcess;

import java.util.ArrayList;

public class DefaultWorkHandler<TIn, TOut> extends ArrayList<IWorkHandler<TIn, TOut>> implements IWorkHandler<TIn, TOut> {

    private final IWorkHandler<TIn, TOut> base;

    public DefaultWorkHandler(IWorkHandler<TIn, TOut> base) {
        this.base = base;
        add(base);
    }

    @Override
    public void onError(Exception exception, Object... args) {
        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onError(exception, args);
            } catch (Exception e) {
                if (handler != base) {
                    base.onError(e, args);
                }
            }
        }
    }

    @Override
    public void onStatusChanged(WorkStatus current, WorkStatus old) {
        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onStatusChanged(current, old);
            } catch (Exception e) {
                onError(e, current, old);
            }
        }
    }

    @Override
    public void onDestroyed() {
        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onDestroyed();
            } catch (Exception e) {
                onError(e);
            }
        }
    }

    @Override
    public void onReading(int index, IDataReader<TIn> reader, String cursor) {
        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onReading(index, reader, cursor);
            } catch (Exception e) {
                onError(e, cursor);
            }
        }
    }

    @Override
    public void onAfterRead(int index, IDataReader<TIn> reader, IBatchData<TIn> batchData) {

        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onAfterRead(index, reader, batchData);
            } catch (Exception e) {
                onError(e, batchData.cursor());
            }
        }
    }

    @Override
    public void onProcessing(int index, IDataProcess<TOut> process, TOut data) {
        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onProcessing(index, process, data);
            } catch (Exception e) {
                onError(e, data);
            }
        }
    }

    @Override
    public void onAfterProcess(int index, IDataProcess<TOut> process, TOut data) {
        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onAfterProcess(index, process, data);
            } catch (Exception e) {
                onError(e, data);
            }
        }
    }

    @Override
    public void onBeforeRead(int index, IDataReader<TIn> reader) {
        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onBeforeRead(index, reader);
            } catch (Exception e) {
                onError(e);
            }
        }
    }

    @Override
    public void onReadEnd(int index, IDataReader<TIn> reader) {
        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onReadEnd(index, reader);
            } catch (Exception e) {
                onError(e);
            }
        }
    }

    @Override
    public void onBeforeProcess(int index, IDataProcess<TOut> process) {
        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onBeforeProcess(index, process);
            } catch (Exception e) {
                onError(e);
            }
        }
    }

    @Override
    public void onProcessEnd(int index, IDataProcess<TOut> process) {
        for (IWorkHandler<TIn, TOut> handler : this) {
            try {
                handler.onProcessEnd(index, process);
            } catch (Exception e) {
                onError(e);
            }
        }
    }

}
