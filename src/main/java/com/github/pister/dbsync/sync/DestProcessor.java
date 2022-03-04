package com.github.pister.dbsync.sync;

import com.github.pister.dbsync.aop.RowInterceptorWrapper;
import com.github.pister.dbsync.config.mapping.table.MappedTable;
import com.github.pister.dbsync.rt.ResultRow;
import com.github.pister.dbsync.rt.Row;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by songlihuang on 2021/1/25.
 */
public interface DestProcessor extends QueryProcessor {

    /**
     * 保存数据，插入或是更新
     * @param mappedTable
     * @param rows
     * @return
     * @throws SQLException
     */
    List<ResultRow> saveRows(MappedTable mappedTable, List<Row> rows, RowInterceptorWrapper rowInterceptorWrapper) throws SQLException;


    List<ResultRow> saveRows(MappedTable mappedTable, List<Row> rows) throws SQLException;

}
