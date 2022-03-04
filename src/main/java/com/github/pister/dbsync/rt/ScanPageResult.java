package com.github.pister.dbsync.rt;


import com.github.pister.dbsync.util.CollectionUtil;

import java.io.Serializable;
import java.util.List;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class ScanPageResult implements Serializable {

    private static final long serialVersionUID = 1251569035206191916L;
    private List<Row> rows;

    private Pagination nextPagination;

    private boolean success;

    private String message;

    public boolean isFinish() {
        return CollectionUtil.isEmpty(rows) && nextPagination == null;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public Pagination getNextPagination() {
        return nextPagination;
    }

    public void setNextPagination(Pagination nextPagination) {
        this.nextPagination = nextPagination;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
