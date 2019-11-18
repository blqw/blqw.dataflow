package com.blqw.work.impl;

import com.blqw.work.core.WorkSettings;

import java.util.UUID;

/**
 * @author blqw
 */
public class SingleBlockingWorkCenter<T> extends BlockingWorkCenter<T, T> {
    public SingleBlockingWorkCenter() {
        super((WorkSettings<T, T>) null);
    }

    public SingleBlockingWorkCenter(WorkSettings<T, T> settings) {
        super(UUID.randomUUID().toString().replace("-", "").toUpperCase(), settings);
    }

    public SingleBlockingWorkCenter(String ticket) {
        super(ticket, null);
    }

    public SingleBlockingWorkCenter(String ticket, WorkSettings<T, T> settings) {
        super(ticket, settings);
    }
}
