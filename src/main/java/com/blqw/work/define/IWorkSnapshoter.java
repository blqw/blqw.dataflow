package com.blqw.work.define;

/**
 * 快照组件
 * @author blqw
 */
public interface IWorkSnapshoter {
    /**
     * 保存快照
     * @param cursors
     */
    void save(String[] cursors);

    /**
     * 读取快照
     * @return
     */
    String[] load();
}
