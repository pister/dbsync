package com.github.pister.dbsync.runtime.scan.generator;

import com.github.pister.dbsync.common.db.MagicDb;
import com.github.pister.dbsync.runtime.exec.RichSql;
import com.github.pister.dbsync.runtime.exec.Row;
import com.github.pister.dbsync.common.tools.util.CollectionUtil;
import com.github.pister.dbsync.common.tools.util.assist.Transformer;
import com.github.pister.dbsync.config.Column;
import com.github.pister.dbsync.config.Columns;
import com.github.pister.dbsync.config.TableConfig;
import com.github.pister.dbsync.runtime.exec.Pagination;
import com.github.pister.dbsync.runtime.exec.SqlContext;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by songlihuang on 2017/7/11.
 */
public abstract class SqlGenerator {

    protected MagicDb magicDb;

    public SqlGenerator(MagicDb magicDb) {
        this.magicDb = magicDb;
    }

    public RichSql generate(TableConfig tableConfig, Pagination pagination, String extSqlCondition) throws SQLException {
        if (pagination == null) {
            return null;
        }
        Columns columns = getColumns(tableConfig);
        SqlContext sqlContext = generateSql(pagination, tableConfig, columns, extSqlCondition);
        if (sqlContext == null) {
            return null;
        }
        RichSql richSql = new RichSql();
        richSql.setParams(sqlContext.getParams());
        richSql.setPkName(columns.getPkName());
        richSql.setSql(sqlContext.getSql());
        richSql.setDbConfig(tableConfig.getDbConfig());
        richSql.setColumns(columns.getColumns());
        return richSql;
    }

    private Columns getColumns(TableConfig tableConfig) throws SQLException {
        return magicDb.getColumns(tableConfig);
    }

    protected abstract SqlContext getCondition(Pagination pagination, TableConfig tableConfig, String pkName, String extSqlCondition) throws SQLException;

    public abstract Pagination getNextPagination(Pagination pagination, List<Row> prevRows);

    private SqlContext generateSql(Pagination pagination, TableConfig tableConfig, Columns columns, String extSqlCondition) throws SQLException {
        SqlContext condition = getCondition(pagination, tableConfig, columns.getPkName(), extSqlCondition);
        if (condition == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select ");
        String fields = CollectionUtil.join(columns.getColumns(), ",", (Transformer<Column, String>) object -> object.getName());
        stringBuilder.append(fields);
        stringBuilder.append(" from ");
        stringBuilder.append(tableConfig.getTableName());
        stringBuilder.append(" where ");
        stringBuilder.append(condition.getSql());
        String sql = stringBuilder.toString();
        SqlContext sqlContext = new SqlContext();
        sqlContext.setSql(sql);
        sqlContext.setParams(condition.getParams());
        return sqlContext;
    }



}
