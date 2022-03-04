package com.github.pister.dbsync.runtime.progress;

import com.github.pister.dbsync.runtime.exec.Pagination;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by songlihuang on 2021/1/25.
 */
public class ProgressUtil {

    public static TableProgress createInit(String taskName, int remoteDbIndex, String remoteTable) {
        TableProgress tableProgress = new TableProgress();
        tableProgress.setTaskName(taskName);
        tableProgress.setRemoteDbIndex(remoteDbIndex);
        tableProgress.setRemoteTable(remoteTable);
        tableProgress.setNextLastModifiedValue(new Date());
        Pagination pagination = new Pagination();
        pagination.setPageSize(20);
        pagination.setLastModified(initGmtModifiedDate());
        tableProgress.setPagination(pagination);
        return tableProgress;
    }

    private static Date initGmtModifiedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1970);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
