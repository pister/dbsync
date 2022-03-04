package com.github.pister.dbsync.sync;

import com.github.pister.dbsync.TransferServer;
import com.github.pister.dbsync.aop.*;
import com.github.pister.dbsync.config.mapping.table.MappedTable;
import com.github.pister.dbsync.db.LocalSequence;
import com.github.pister.dbsync.progress.ProgressUtil;
import com.github.pister.dbsync.progress.TableProgress;
import com.github.pister.dbsync.rt.ResultRow;
import com.github.pister.dbsync.rt.Row;
import com.github.pister.dbsync.rt.ScanPageResult;
import com.github.pister.dbsync.util.CollectionUtil;
import com.github.pister.dbsync.progress.ProgressManager;
import com.github.pister.dbsync.rt.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2021/1/25.
 */
public class TableSyncProcessor {

    private static final Logger log = LoggerFactory.getLogger(TableSyncProcessor.class);

    private TransferServer transferServer;

    private ProgressManager progressManager;

    private DestProcessor destProcessor;

    private Map<String, MappedTable> tables;

    public TableSyncProcessor(TransferServer transferServer,
                              DestProcessor destProcessor, ProgressManager progressManager,
                              Map<String, MappedTable> tables) {
        this.transferServer = transferServer;
        this.destProcessor = destProcessor;
        this.progressManager = progressManager;
        this.tables = tables;
    }

    private List<Row> interceptOnBefore(List<Row> rows, AopContext aopContext, BatchInterceptor batchInterceptor) throws SQLException {
        if (batchInterceptor == null) {
            return rows;
        }
        if (CollectionUtil.isEmpty(rows)) {
            return rows;
        }
        List<Row> ret = CollectionUtil.newArrayList(rows.size());
        for (Row row : rows) {
            InterceptorResult interceptorResult = batchInterceptor.onBefore(row, aopContext);
            if (interceptorResult.isIgnore()) {
                continue;
            }
            for (Row newRow : interceptorResult.getRows()) {
                ret.add(newRow);
            }
        }
        return ret;
    }

    private void interceptOnAfter(List<ResultRow> resultRows, AopContext aopContext, BatchInterceptor batchInterceptor) throws SQLException {
        if (batchInterceptor == null) {
            return;
        }
        if (CollectionUtil.isEmpty(resultRows)) {
            return;
        }
        batchInterceptor.onAfter(resultRows, aopContext);
    }

    public int syncTable(String taskName, ProcessListener processListener, LocalSequence localSequence) throws Exception {
        log.warn("start sync task: " + taskName);
        MappedTable mappedTable = tables.get(taskName);
        if (mappedTable == null) {
            log.error("task not exist:" + taskName);
            return 0;
        }
        TableProgress tableProgress = progressManager.load(taskName);
        if (tableProgress == null) {
            log.warn(mappedTable.getRemoteDbIndex() + "/" + mappedTable.getRemoteTable() + " not exist, create init.");
            tableProgress = ProgressUtil.createInit(taskName, mappedTable.getRemoteDbIndex(), mappedTable.getRemoteTable());
        } else if (mappedTable.isOnlyFullDump()) {
            log.warn("task is only full dump: " + taskName);
            tableProgress = ProgressUtil.createInit(taskName, tableProgress.getRemoteDbIndex(), tableProgress.getRemoteTable());
        }
        log.warn("task " + taskName + "'s progress: " + tableProgress.getPagination() + ", nextLastModifiedValue:" + tableProgress.getNextLastModifiedValue());

        BatchInterceptor batchInterceptor = mappedTable.getBatchInterceptor();
        Pagination pagination = tableProgress.getPagination();
        AopContext aopContext = new DefaultAopContext(transferServer.createQueryProcessor(), destProcessor, localSequence);
        int rowCount = 0;
        RowInterceptorWrapper rowInterceptorWrapper = null;
        if (mappedTable.getRowInterceptor() != null) {
            rowInterceptorWrapper = new RowInterceptorWrapper(mappedTable.getRowInterceptor(), aopContext);
        }
        while (true) {
            ScanPageResult scanPageResult = transferServer.fetchForPage(tableProgress.getRemoteDbIndex(), tableProgress.getRemoteTable(), pagination, mappedTable.getSourceExtCondition());
            if (!scanPageResult.isSuccess()) {
                log.error("error:" + scanPageResult.getMessage());
                // 如果失败，1分钟后再试
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            List<Row> rows = scanPageResult.getRows();
            rows = interceptOnBefore(rows, aopContext, batchInterceptor);
            List<ResultRow> resultRowList = destProcessor.saveRows(mappedTable, rows, rowInterceptorWrapper);
            rowCount += resultRowList.size();
            interceptOnAfter(resultRowList, aopContext, batchInterceptor);
            if (scanPageResult.isFinish()) {
                saveProgress(tableProgress, pagination, true);
                processListener.onFinish(taskName);
                log.warn("transfer [" + taskName + "] finish, total rows: " + rowCount);
                return rowCount;
            }

            pagination = scanPageResult.getNextPagination();
            if (rowCount % 500 == 0) {
                saveProgress(tableProgress, pagination, false);
                if (rowCount > 0) {
                    log.warn("transfer [" + taskName + "] rows: " + rowCount);
                }
            }
            processListener.onProcess(taskName, rowCount);
        }
    }

    private void saveProgress(TableProgress tableProgress, Pagination pagination, boolean syncFinish) throws IOException {
        if (syncFinish) {
            Date nextLastModified = tableProgress.getNextLastModifiedValue();
            pagination.setLastModified(nextLastModified);
            pagination.setValue(null);
            tableProgress.setNextLastModifiedValue(new Date());
        }
        tableProgress.setPagination(pagination);
        progressManager.save(tableProgress.getTaskName(), tableProgress);
    }
}
