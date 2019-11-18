package com.blqw.dataflow.define;

/**
 * 缓存组件
 * @author blqw
 */
public interface IStringCache {
    /**
     * 获取指定键的缓存
     * @param key          缓存的键
     */
    String get(String key);

    /**
     * 设置缓存
     *
     * @param key          缓存的键
     * @param value        缓存的值
     * @param expireSecond 缓存过期时间
     */
    void set(String key, String value, int expireSecond);

    /**
     * 查询指定键的缓存是否存在
     * @param key          缓存的键
     *
     * @return
     */
    boolean has(String key);

    /**
     * 移除指定键的缓存
     *
     * @param key 缓存key
     */
    void remove(String key);

    /**
     * 释放缓存组件
     */
    void release();

    /**
     * 返回指定组件是否已释放
     *
     * @return
     */
    boolean isReleased();
}
