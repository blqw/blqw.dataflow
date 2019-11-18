package com.blqw.dataflow.define;

/**
 * 数据输出组件
 * @author blqw
 */
@FunctionalInterface
public interface IDataOutput<T> {
    /**
     * 输出指定数量的一批数据
     */
    IBatchData<T> get(String cursor, Integer size);
}
