package com.github.pister.dbsync;


import com.github.pister.dbsync.row.RowProcessor;
import com.github.pister.dbsync.config.Columns;
import com.github.pister.dbsync.rt.Pagination;
import com.github.pister.dbsync.rt.ScanPageResult;
import com.github.pister.dbsync.sync.QueryProcessor;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by songlihuang on 2017/7/12.
 */
public interface TransferServer {

    List<String> dbNameList();

    List<String> tableNameList(int dbIndex);

    <T> List<T> executeForList(int dbIndex, RowProcessor<T> rowProcessor, String sql, Object[] params) throws SQLException;

    <T> T executeForObject(int dbIndex, RowProcessor<T> rowProcessor, String sql, Object[] params) throws SQLException;

    Columns getColumns(int dbIndex, String tableName);

    ScanPageResult fetchForPage(int dbIndex, String tableName, Pagination pagination, String extSqlCondition);

    QueryProcessor createQueryProcessor();
}
