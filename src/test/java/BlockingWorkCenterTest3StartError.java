import com.alibaba.fastjson.JSONObject;
import com.blqw.dataflow.define.IBatchData;
import com.blqw.dataflow.impl.BatchData;
import org.junit.jupiter.api.Test;
import com.blqw.work.core.WorkCanceledException;
import com.blqw.work.core.WorkException;
import com.blqw.work.core.WorkSettings;
import com.blqw.work.core.WorkStatus;
import com.blqw.work.define.IWorkHandler;
import com.blqw.work.define.IDataReader;
import com.blqw.work.impl.SingleBlockingWorkCenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class BlockingWorkCenterTest3StartError {

    @Test
    void start() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        WorkSettings<JSONObject, JSONObject> settings = new WorkSettings<>();
        settings.setReaders(
                new TestReader(00000, "A"),
                new TestReader(10000, "B"),
                new TestReader(20000, "C"),
                new TestReader(30000, "D"),
                new TestReader(40000, "E"),
                new TestReader(50000, "F"),
                new TestReader(60000, "G")
        );
        settings.setProcesses(data -> {
            counter.incrementAndGet();
            System.out.println(data.toJSONString());
        });
        settings.setKeyFetcher(x -> x.getString("ID"));

        SingleBlockingWorkCenter<JSONObject> workCenter = new SingleBlockingWorkCenter<>();
        workCenter.configuration(settings);
        workCenter.setHandler(new IWorkHandler<JSONObject, JSONObject>() {
            @Override
            public void onStatusChanged(WorkStatus current, WorkStatus old) {
                if (current == WorkStatus.running) {
                    throw new WorkCanceledException("启动失败");
                }
            }
        });
        try {
            workCenter.startAsync(4, 10);
            assert false;
        } catch (Exception e) {
            assert e instanceof WorkException;
        }

        workCenter.await();
        assert counter.intValue() == 0;
        System.out.println("完成");
    }

    static class TestReader implements IDataReader<JSONObject> {
        private final int start;
        private final int end;
        private final String prefix;

        public TestReader(int start, String prefix) {
            this.start = start;
            this.end = start + 999;
            this.prefix = prefix;
        }


        @Override
        public IBatchData<JSONObject> get(String cursor, Integer size) {
            size = size == null ? 94 : size;
            final int id = cursor == null || cursor.length() == 0 ? start : Integer.parseInt(cursor) + 1;
            if (id >= end) {
                return BatchData.end;
            }
            List<JSONObject> data = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if (id + i > end) {
                    break;
                }
                data.add(new JSONObject().fluentPut("ID", id + i).fluentPut("NAME", prefix + ":" + (id + i)));
            }
            return new BatchData<>(data.size() < size, data.get(data.size() - 1).getString("ID"), data);
        }
    }
}