package com.blqw.work.core;

/**
 * 导致工作取消的异常
 * @author blqw
 */
public class WorkCanceledException extends WorkException {

    public WorkCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkCanceledException(Throwable cause) {
        super(cause);
    }

    public WorkCanceledException(String message) {
        super(message);
    }
}