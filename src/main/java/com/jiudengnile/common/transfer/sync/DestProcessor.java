package com.jiudengnile.common.transfer.sync;

import com.jiudengnile.common.transfer.aop.RowInterceptorWrapper;
import com.jiudengnile.common.transfer.config.mapping.table.MappedTable;
import com.jiudengnile.common.transfer.rt.ResultRow;
import com.jiudengnile.common.transfer.rt.Row;
import wint.help.sql.RowProcessor;

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
