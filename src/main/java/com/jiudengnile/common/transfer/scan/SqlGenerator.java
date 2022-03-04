package com.jiudengnile.common.transfer.scan;

import com.jiudengnile.common.transfer.config.Column;
import com.jiudengnile.common.transfer.config.Columns;
import com.jiudengnile.common.transfer.config.TableConfig;
import com.jiudengnile.common.transfer.rt.Pagination;
import com.jiudengnile.common.transfer.rt.RichSql;
import com.jiudengnile.common.transfer.rt.Row;
import com.jiudengnile.common.transfer.rt.SqlContext;
import wint.lang.magic.Transformer;
import wint.lang.utils.CollectionUtil;

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
        String fields = CollectionUtil.join(columns.getColumns(), ",", new Transformer<Column, String>() {
            @Override
            public String transform(Column object) {
                return object.getName();
            }
        });
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
