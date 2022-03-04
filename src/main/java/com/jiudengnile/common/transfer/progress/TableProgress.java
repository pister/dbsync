package com.jiudengnile.common.transfer.progress;

import com.jiudengnile.common.transfer.rt.Pagination;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class TableProgress implements Serializable {

    private static final long serialVersionUID = -6059050810149673826L;

    private String taskName;

    private int remoteDbIndex;

    private String remoteTable;

    /**
     * 下一次增量的时间值
     */
    private Date nextLastModifiedValue;

    private Pagination pagination;

    public int getRemoteDbIndex() {
        return remoteDbIndex;
    }

    public void setRemoteDbIndex(int remoteDbIndex) {
        this.remoteDbIndex = remoteDbIndex;
    }

    public String getRemoteTable() {
        return remoteTable;
    }

    public void setRemoteTable(String remoteTable) {
        this.remoteTable = remoteTable;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Date getNextLastModifiedValue() {
        return nextLastModifiedValue;
    }

    public void setNextLastModifiedValue(Date nextLastModifiedValue) {
        this.nextLastModifiedValue = nextLastModifiedValue;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
