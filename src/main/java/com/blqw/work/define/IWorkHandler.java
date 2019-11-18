package com.blqw.work.define;

import com.blqw.dataflow.define.IBatchData;
import com.blqw.work.core.WorkStatus;

/**
 * 工作事件处理器
 *
 * @author blqw
 */
public interface IWorkHandler<TIn, TOut> {

    /**
     * 当出现异常时触发
     *
     * @param exception 异常
     * @param args      相关参数
     */
    default void onError(Exception exception, Object... args) {

    }

    /**
     * 数据流出组件启动时触发
     *
     * @param index
     * @param process
     */
    default void onBeforeProcess(int index, IDataProcess<TOut> process) {

    }

    /**
     * 当数据流出组件执行时触发
     *
     * @param index
     * @param process process组件
     * @param data
     */
    default void onProcessing(int index, IDataProcess<TOut> process, TOut data) {

    }

    /**
     * 当数据流出组件执行后触发
     *
     * @param index
     * @param process process组件
     * @param data
     */
    default void onAfterProcess(int index, IDataProcess<TOut> process, TOut data) {

    }

    /**
     * 数据流出组件停止时触发
     *
     * @param index
     * @param process
     */
    default void onProcessEnd(int index, IDataProcess<TOut> process) {

    }

    /**
     * 数据流入组件启动时触发
     *
     * @param index
     * @param reader
     */
    default void onBeforeRead(int index, IDataReader<TIn> reader) {

    }

    /**
     * 数据流入组件执行时触发
     *
     * @param index
     * @param reader
     * @param cursor
     */
    default void onReading(int index, IDataReader<TIn> reader, String cursor) {

    }

    /**
     * 数据流入组件执行后触发
     *
     * @param index
     * @param reader
     * @param batchData
     */
    default void onAfterRead(int index, IDataReader<TIn> reader, IBatchData<TIn> batchData) {

    }

    /**
     * 数据流入组件停止时触发
     *
     * @param reader
     */
    default void onReadEnd(int index, IDataReader<TIn> reader) {

    }


    /**
     * 工作中间销毁时触发
     */
    default void onDestroyed() {

    }

    /**
     * 工作中心状态改变时触发
     *
     * @param current
     * @param old
     */
    default void onStatusChanged(WorkStatus current, WorkStatus old) {

    }
}
