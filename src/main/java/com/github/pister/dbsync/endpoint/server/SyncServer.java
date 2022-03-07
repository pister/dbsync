package com.github.pister.dbsync.endpoint.server;


import com.github.pister.dbsync.config.Columns;
import com.github.pister.dbsync.runtime.exec.Pagination;
import com.github.pister.dbsync.runtime.exec.ScanPageResult;
import com.github.pister.dbsync.runtime.sync.QueryProcessor;

import java.util.List;

/**
 * Created by songlihuang on 2017/7/12.
 */
public interface DbSyncServer {

    List<String> dbNameList();

    List<String> tableNameList(int dbIndex);

    Columns getColumns(int dbIndex, String tableName);

    ScanPageResult fetchForPage(int dbIndex, String tableName, Pagination pagination, String extSqlCondition);

    QueryProcessor createQueryProcessor();
}
