package com.blqw.work.define;

import com.blqw.work.core.WorkSettings;
import com.blqw.work.core.WorkStatus;

/**
 * 工作中心组件
 *
 * @author blqw
 */
public interface IWorkCenter<TIn, TOut> {

    /**
     * 配置工作中心
     */
    void configuration(WorkSettings<TIn, TOut> settings);

    /**
     * 设置处理程序
     */
    void setHandler(IWorkHandler<TIn, TOut> handler);

    /**
     * 开始工作
     *
     * @param readThreads  输入线程数
     * @param processThreads 输出线程数
     */
    void startAsync(int readThreads, int processThreads);

    /**
     * 获取工作中心当前状态
     *
     * @return 工作中心当前状态
     */
    WorkStatus getStatus();

    /**
     * 获取最后一个错误
     *
     * @return 最后一个错误
     */
    Exception getLastException();

    /**
     * 摧毁一个工作中心,使他不在工作
     */
    void destroy();

    /**
     * 等待工作中心执行完成或取消
     */
    void await();
}