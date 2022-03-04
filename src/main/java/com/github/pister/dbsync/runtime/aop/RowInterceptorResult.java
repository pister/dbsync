package com.github.pister.dbsync.runtime.aop;

import com.github.pister.dbsync.runtime.exec.Row;

/**
 * Created by songlihuang on 2021/2/19.
 */
public class RowInterceptorResult {

    /**
     * 最终需要写入的数据
     */
    private Row row;

    /**
     * 是否忽略这些数据（true将会忽略）
     */
    private boolean ignore;

    public RowInterceptorResult(boolean ignore, Row row) {
        this.ignore = ignore;
        this.row = row;
    }

    public static RowInterceptorResult createIgnoredResult() {
        return new RowInterceptorResult(true, null);
    }

    public Row getRow() {
        return row;
    }

    public boolean isIgnore() {
        return ignore;
    }
}

