package com.blqw.work.core;


/**
 * 表示一个游标
 *
 * @author blqw
 */
public final class DataCursor {
    public final int index;
    public final String cursor;
    public final Object data;

    public DataCursor(int index, String cursor, Object data) {
        this.index = index;
        this.cursor = cursor;
        this.data = data;
    }
}
