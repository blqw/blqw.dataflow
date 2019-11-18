package com.blqw.work.core;

import com.blqw.dataflow.define.*;
import com.blqw.work.define.IDataReader;
import com.blqw.work.define.IDataProcess;
import com.blqw.work.define.IWorkSnapshoter;

import java.util.HashMap;

/**
 * 工作中心设置
 * @author blqw
 */
public final class WorkSettings<TIn, TOut> {

    private IDataReader<TIn>[] readers;
    private IDataProcess<TOut>[] processes;
    private IDataKeyFetcher<TOut> keyfetcher;
    private IStringCache cache;
    private IWorkSnapshoter snapshoter;
    private HashMap<String, Object> properties;
    private IDataTransform<TIn, TOut> transform;

    public WorkSettings() {
        properties = new HashMap<>();
    }

    public WorkSettings<TIn, TOut> cloneSettings() {
        WorkSettings<TIn, TOut> settings = new WorkSettings<>();
        settings.readers = this.readers;
        settings.processes = this.processes;
        settings.keyfetcher = this.keyfetcher;
        settings.cache = this.cache;
        settings.snapshoter = this.snapshoter;
        settings.properties.putAll(this.properties);
        return settings;
    }

    public final void setReaders(IDataReader<TIn>... readers) {
        this.readers = readers;
    }

    public final void setProcesses(IDataProcess<TOut>... processes) {
        this.processes = processes;
    }

    public void setKeyFetcher(IDataKeyFetcher<TOut> keyfetcher) {
        this.keyfetcher = keyfetcher;
    }

    public void setCache(IStringCache cache) {
        this.cache = cache;
    }

    public void setSnapshoter(IWorkSnapshoter snapshoter) {
        this.snapshoter = snapshoter;
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public Object getProperty(String name, Object defaultValue) {
        Object value = getProperty(name);
        return value == null ? defaultValue : value;
    }

    public IDataReader<TIn>[] getReaders() {
        return readers;
    }

    public IDataKeyFetcher<TOut> getKeyfetcher() {
        return keyfetcher;
    }

    public IDataProcess<TOut>[] getProcesses() {
        return processes;
    }

    public IStringCache getCache() {
        return cache;
    }

    public IWorkSnapshoter getSnapshoter() {
        return snapshoter;
    }

    public IDataTransform<TIn, TOut> getDataTransform() {
        return transform;
    }

    public void setDataTransform(IDataTransform<TIn, TOut> transform) {
        this.transform = transform;
    }
}
