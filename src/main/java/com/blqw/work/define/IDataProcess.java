package com.blqw.work.define;

import com.blqw.dataflow.define.IDataInput;

/**
 * 数据处理组件
 *
 * @param <T>
 * @author blqw
 */
@FunctionalInterface
public interface IDataProcess<T> extends IDataInput<T> {

}
