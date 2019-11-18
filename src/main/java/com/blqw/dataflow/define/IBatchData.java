package com.blqw.dataflow.define;

import java.util.List;

/**
 * 一批数据
 * @author blqw
 */
public interface IBatchData<T> {

    /**
     * 表示最后一批数据
     */
    boolean isEnd();

    /**
     * 下一批数据的游标
     */
    String cursor();

    /**
     * 数据
     */
    List<T> data();
}
