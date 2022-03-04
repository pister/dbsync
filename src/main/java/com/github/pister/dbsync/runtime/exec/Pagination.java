package com.github.pister.dbsync.runtime.exec;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class Pagination implements Serializable {

    private static final long serialVersionUID = 8604299651117636204L;

    private int pageSize = 20;

    /**
     * 用于断点
     */
    private Object value;

    /**
     * 用于增量
     */
    private Date lastModified;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "Pagination{" +
                "pageSize=" + pageSize +
                ", value=" + value +
                ", lastModified=" + lastModified +
                '}';
    }
}
