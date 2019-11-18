package com.blqw.work.core;

/**
 * 工作状态
 *
 * @author blqw
 */
public enum WorkStatus {
    /**
     * 准备中
     */
    preparation(0, "准备中"),
    /**
     * 就绪
     */
    ready(1, "就绪"),
    /**
     * 运行中
     */
    running(2, "运行中"),
    /**
     * 完成
     */
    completed(3, "完成"),
    /**
     * 已销毁
     */
    destroyed(4, "已销毁"),
    /**
     * 取消
     */
    canceled(5, "取消"),
    ;

    WorkStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public final int code;
    public final String desc;

    @Override
    public String toString() {
        return "[" + code + "]" + desc;
    }
}
