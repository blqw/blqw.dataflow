package com.blqw.work.core;

import com.blqw.work.define.IWorkHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 状态机
 *
 * @author blqw
 */
public final class WorkStatusMachine {
    private final IWorkHandler handler;
    private WorkStatus status = WorkStatus.preparation;

    static Map<WorkStatus, WorkStatus[]> map = new HashMap<WorkStatus, WorkStatus[]>() {{
        put(WorkStatus.preparation, new WorkStatus[]{WorkStatus.preparation, WorkStatus.ready, WorkStatus.canceled, WorkStatus.destroyed});
        put(WorkStatus.ready, new WorkStatus[]{WorkStatus.running, WorkStatus.preparation, WorkStatus.canceled, WorkStatus.destroyed});
        put(WorkStatus.running, new WorkStatus[]{WorkStatus.completed, WorkStatus.canceled, WorkStatus.destroyed});
        put(WorkStatus.completed, new WorkStatus[]{});
        put(WorkStatus.destroyed, new WorkStatus[]{});
        put(WorkStatus.canceled, new WorkStatus[]{});
    }};

    public WorkStatusMachine(IWorkHandler handler) {
        this.handler = handler;
    }

    /**
     * 改变状态机的当前状态
     *
     * @param newStatus 新状态
     * @param runnable  改变成功后执行的回调
     * @return 返回是否变更成功
     */
    public synchronized boolean change(WorkStatus newStatus, Runnable runnable) {
        WorkStatus[] nexts = map.get(status);
        if (contains(nexts, newStatus) && change(newStatus)) {
            if (runnable != null) {
                runnable.run();
            }
            return true;
        }
        return false;
    }

    private boolean change(WorkStatus newStatus) {
        if (this.status == newStatus) {
            return true;
        }
        WorkStatus old = this.status;
        this.status = newStatus;
        handler.onStatusChanged(newStatus, old);
        return this.status == newStatus;
    }

    private boolean contains(WorkStatus[] next, WorkStatus status) {
        for (WorkStatus n : next) {
            if (n == status) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回当前状态机的状态
     *
     * @return
     */
    public WorkStatus get() {
        return status;
    }
}
