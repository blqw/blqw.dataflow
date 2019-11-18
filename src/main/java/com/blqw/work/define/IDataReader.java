package com.blqw.work.define;

import com.blqw.dataflow.define.IDataOutput;

/**
 * 数据读取组件
 *
 * @author blqw
 * @param <T>
 */
@FunctionalInterface
public interface IDataReader<T> extends IDataOutput<T> {
}
