package com.jiudengnile.common.transfer.sync;

import com.jiudengnile.common.transfer.config.mapping.table.MappedTable;
import wint.help.sql.RowProcessor;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by songlihuang on 2021/3/12.
 */
public interface QueryProcessor {

    /**
     * 在目标库执行一条查询语句返回条数据，
     * 支持分库分表，routeValue 为路由字段值，
     * 在分库分表的时候sql语句中表名请使用 $table$ 代替，方法会自动计算表名并填入，比如：
     *  select a, b, c from $table$ where id = ?
     *  会根据路由规则自动变成，诸如
     *  select a, b, c from my_table_0002 where id = ?
     * @param mappedTable
     * @param rowProcessor
     * @param sql
     * @param routeValue
     * @param args
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T queryForObject(MappedTable mappedTable, RowProcessor<T> rowProcessor, String sql, Object routeValue, Object... args) throws SQLException;

    /**
     * 在目标库执行一条查询语句返回多条数据
     * 支持分库分表，routeValue 为路由字段值，
     * 在分库分表的时候sql语句中表名请使用 $table$ 代替，方法会自动计算表名并填入，比如：
     *  select a, b, c from $table$ where id = ?
     *  会根据路由规则自动变成，诸如
     *  select a, b, c from my_table_0002 where id = ?
     * @param mappedTable
     * @param mappedTable
     * @param rowProcessor
     * @param sql
     * @param routeValue
     * @param args
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> queryForList(MappedTable mappedTable, RowProcessor<T> rowProcessor, String sql, Object routeValue, Object... args) throws SQLException;

}
