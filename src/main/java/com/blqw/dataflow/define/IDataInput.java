package com.blqw.dataflow.define;

/**
 * 数据输入组件
 * @author blqw
 */
@FunctionalInterface
public interface IDataInput<T> {
    /**
     * 输入数据
     */
    void set(T data);
}
