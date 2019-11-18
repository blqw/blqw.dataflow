package com.blqw.dataflow.define;

/**
 * 数据主键提取器
 * @author blqw
 */
@FunctionalInterface
public interface IDataKeyFetcher<T> {
    /**
     * 返回数据的主键信息
     */
    String getKey(T data);
}
