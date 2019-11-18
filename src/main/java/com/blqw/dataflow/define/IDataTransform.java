package com.blqw.dataflow.define;

/**
 * 数据转换
 * @author blqw
 */
@FunctionalInterface
public interface IDataTransform<TIn, TOut> {
    TOut to(TIn input);
}
