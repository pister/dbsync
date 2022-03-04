package com.jiudengnile.common.transfer;


import com.jiudengnile.common.transfer.config.Columns;
import com.jiudengnile.common.transfer.rt.Pagination;
import com.jiudengnile.common.transfer.rt.ScanPageResult;
import com.jiudengnile.common.transfer.sync.QueryProcessor;
import wint.help.sql.RowProcessor;

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
