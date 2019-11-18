package com.blqw.dataflow.core;

import com.blqw.dataflow.define.IStringCache;
import com.blqw.work.core.WorkCanceledException;

import java.util.concurrent.*;


/**
 * 内存缓存, 存放在HashMap里
 *
 * @author blqw
 */
public class MemoryCache implements IStringCache {
    static class CacheItem {
        final String name;
        String value;
        long timeout;

        CacheItem(String name, String value, long timeout) {
            this.name = name;
            this.value = value;
            this.timeout = timeout;
        }
    }

    private final ConcurrentMap<String, CacheItem> map;
    private ScheduledExecutorService timer;

    public MemoryCache() {
        map = new ConcurrentHashMap<>(256);
        timer = Executors.newScheduledThreadPool(1);
        timer.scheduleAtFixedRate(this::clearExpired, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public String get(String key) {
        CacheItem item = map.getOrDefault(key, null);
        if (item == null) {
            return null;
        }
        if (item.timeout < System.currentTimeMillis()) {
            map.remove(key, item);
            return null;
        }
        return item.value;
    }

    @Override
    public void set(String key, String value, int expireSecond) {
        if (isReleased()) {
            throw new WorkCanceledException("缓存已释放");
        }
        map.put(key, new CacheItem(key, value, System.currentTimeMillis() + expireSecond * 1000));
    }

    @Override
    public boolean has(String key) {
        return get(key) != null;
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public void release() {
        if (this.timer == null) {
            return;
        }
        ScheduledExecutorService timer;
        synchronized (this.timer) {
            if (this.timer == null) {
                return;
            }
            timer = this.timer;
            this.timer = null;
        }
        timer.shutdown();
        map.clear();
    }

    @Override
    public boolean isReleased() {
        return timer == null;
    }

    public void clearExpired() {
        Object[] keys = map.keySet().toArray();
        for (Object key : keys) {
            get((String) key);
        }
    }
}
