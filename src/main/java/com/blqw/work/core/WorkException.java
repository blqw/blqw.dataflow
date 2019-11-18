package com.blqw.work.core;

/**
 * 工作中的异常
 * @author blqw
 */
public class WorkException extends RuntimeException {

    public WorkException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkException(Throwable cause) {
        super(cause);
    }

    public WorkException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        if (id == null) {
            return super.getMessage();
        }
        return "errno:" + id + "," + super.getMessage();
    }

    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}