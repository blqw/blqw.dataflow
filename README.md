# dataflow
## ★ 简介
提供一种方便操作批量数据的框架  
实现需要自己写

## ★ 主要操作
```java
// 创建工作参数
WorkSettings<JSONObject, JSONObject> settings = new WorkSettings<>();
// 设置参数
settings.setReaders(...);    // 设置读取数据的组件
settings.setProcesses(...);  // 设置处理数据的组件
settings.setKeyFetcher(...); // ID提取组件 (可选, 用于任务异常后重启的幂等判断)
settings.setCache(...);      // 缓存组件 (可选, 用于任务异常后重启的幂等判断)
settings.setSnapshoter(...); // 快照组件 (可选, 用于任务异常后重启的游标设置)
settings.setProperty(name, value);   // 设置拓展属性 (可选)

// 创建工作中心
SingleBlockingWorkCenter<JSONObject> workCenter = new SingleBlockingWorkCenter<>(settings);
// 开始工作(读取线程数, 处理线程数)
workCenter.startAsync(4, 10);
// 等待工作完成
workCenter.await();
```
## ★ 组件介绍

### 1. `IDataReader<T>`(读取数据组件)
[>源码<](src/main/java/com/blqw/work/define/IDataReader.java)  
> 从任意位置读取指定长度的数据和游标，并可根据游标继续读取下一批数据

```java
@FunctionalInterface
public interface IDataReader<T> extends IDataOutput<T> {
}

@FunctionalInterface
public interface IDataOutput<T> {
    IBatchData<T> get(String cursor, Integer size);
}

public interface IBatchData<T> {
    boolean isEnd();
    String cursor();
    List<T> data();
}
```

### 2. `IDataProcess<T>`(数据处理组件)
[>源码<](src/main/java/com/blqw/work/define/IDataProcess.java)  
> 用于处理单个数据, 如果处理失败直接抛出异常
```java
@FunctionalInterface
public interface IDataProcess<T> extends IDataInput<T> {

}
@FunctionalInterface
public interface IDataInput<T> {
    void set(T data);
}
```

### 3. `IDataKeyFetcher<T>`(主键提取器)
[>源码<](src/main/java/com/blqw/work/define/IDataKeyFetcher.java)  
> 用于返回数据中的唯一标识
```java
@FunctionalInterface
public interface IDataKeyFetcher<T> {
    String getKey(T data);
}
```

### 4. `IStringCache`(缓存组件)
[>源码<](src/main/java/com/blqw/work/define/IStringCache.java)  
> 用于暂时缓存已经处理的数据唯一标识
```java
public interface IStringCache {
    String get(String key);
    void set(String key, String value, int expireSecond);
    boolean has(String key);
    void remove(String key);
    void release();
    boolean isReleased();
}
```

### 5. `IWorkSnapshoter`(快照组件)
[>源码<](src/main/java/com/blqw/work/define/IWorkSnapshoter.java)  
> 用于保存读取数据的游标
``` java
public interface IWorkSnapshoter {
    void save(String[] cursors);
    String[] load();
}
```

## ☆ 一段测试demo
[>源码<](src/test/java/BlockingWorkCenterTest.java)  
输出
```text
2019-11-18 16:49:04.568  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> 状态变更:[0]准备中 -> [1]就绪
2019-11-18 16:49:04.571  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> 状态变更:[1]就绪 -> [2]运行中
2019-11-18 16:49:04.576  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[0] run.
2019-11-18 16:49:04.576  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[2] run.
2019-11-18 16:49:04.576  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[1] run.
2019-11-18 16:49:04.576  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[3] run.
2019-11-18 16:49:04.576  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[0] run.
2019-11-18 16:49:04.577  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[1] run.
2019-11-18 16:49:04.577  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[2] run.
2019-11-18 16:49:04.577  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[3] run.
2019-11-18 16:49:04.577  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[4] run.
2019-11-18 16:49:04.577  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[5] run.
2019-11-18 16:49:04.577  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[6] run.
2019-11-18 16:49:04.577  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[7] run.
2019-11-18 16:49:04.577  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[8] run.
2019-11-18 16:49:04.577  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[9] run.
2019-11-18 16:49:04.585  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[4] run.
2019-11-18 16:49:04.585  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[5] run.
2019-11-18 16:49:04.587  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[6] run.
2019-11-18 16:49:04.603  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[0] exit.
2019-11-18 16:49:04.604  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[1] exit.
2019-11-18 16:49:04.605  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[2] exit.
2019-11-18 16:49:04.606  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[4] exit.
2019-11-18 16:49:04.607  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[3] exit.
2019-11-18 16:49:04.607  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[6] exit.
2019-11-18 16:49:04.611  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> reader[5] exit.
2019-11-18 16:49:04.611  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[2] exit.
2019-11-18 16:49:04.611  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[4] exit.
2019-11-18 16:49:04.611  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[6] exit.
2019-11-18 16:49:09.612  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[0] exit.
2019-11-18 16:49:09.612  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[3] exit.
2019-11-18 16:49:09.612  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[9] exit.
2019-11-18 16:49:09.612  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[7] exit.
2019-11-18 16:49:09.612  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[1] exit.
2019-11-18 16:49:09.612  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[5] exit.
2019-11-18 16:49:09.612  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> 状态变更:[2]运行中 -> [3]完成
2019-11-18 16:49:09.612  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> process[8] exit.
2019-11-18 16:49:09.614  com.blqw.work.impl.BlockingWorkCenter[2E23D1E3A9964510B69909A1EDDA2240] >> 退出...
```

## 更新说明 
#### [1.0.0.0] 2018.04.17
* 初始版本