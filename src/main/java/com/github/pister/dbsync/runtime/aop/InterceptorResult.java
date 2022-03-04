package com.github.pister.dbsync.runtime.aop;

import com.github.pister.dbsync.runtime.exec.Row;

/**
 * Created by songlihuang on 2021/2/19.
 */
public class InterceptorResult {

    /**
     * 最终需要写入的数据
     */
    private Row[] rows;

    /**
     * 是否忽略这些数据（true将会忽略）
     */
    private boolean ignore;

    public InterceptorResult(boolean ignore, Row... rows) {
        this.ignore = ignore;
        this.rows = rows;
    }

    public Row[] getRows() {
        return rows;
    }

    public boolean isIgnore() {
        return ignore;
    }
}

