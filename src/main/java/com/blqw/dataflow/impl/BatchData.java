package com.blqw.dataflow.impl;

import com.blqw.dataflow.define.IBatchData;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示一批数据
 *
 * @param <T>
 * @author blqw
 */
public class BatchData<T> implements IBatchData<T> {

    public static IBatchData end = new IBatchData() {
        @Override
        public boolean isEnd() {
            return true;
        }

        @Override
        public String cursor() {
            return "";
        }

        @Override
        public List data() {
            return new ArrayList();
        }
    };

    public final boolean isEnd;
    public final String cursor;
    public final List<T> data;

    public BatchData(boolean isEnd, String cursor, List<T> data) {
        this.isEnd = isEnd;
        this.cursor = cursor;
        this.data = data;
    }

    @Override
    public boolean isEnd() {
        return isEnd;
    }

    @Override
    public String cursor() {
        return cursor;
    }

    @Override
    public List<T> data() {
        return data;
    }
}
