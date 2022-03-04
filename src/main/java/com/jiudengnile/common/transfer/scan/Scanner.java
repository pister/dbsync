package com.jiudengnile.common.transfer.scan;

import com.jiudengnile.common.transfer.config.Column;
import com.jiudengnile.common.transfer.config.Columns;
import com.jiudengnile.common.transfer.config.TableConfig;
import com.jiudengnile.common.transfer.rt.Pagination;
import com.jiudengnile.common.transfer.rt.RichSql;
import com.jiudengnile.common.transfer.rt.Row;
import com.jiudengnile.common.transfer.rt.ScanPageResult;
import wint.help.sql.TypeUtil;
import wint.lang.utils.MapUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class Scanner {

    private MagicDb magicDb;

    public Scanner(MagicDb magicDb) {
        this.magicDb = magicDb;
    }

    private ConcurrentMap<String, SqlGenerator> tableSqlGeneratorCache = MapUtil.newConcurrentHashMap();

    public ScanPageResult fetchForPage(Pagination pagination, TableConfig tableConfig, String extSqlCondition) throws SQLException {
        SqlGenerator sqlGenerator = getSqlGenerator(tableConfig);
        ScanPageResult scanPageResult = new ScanPageResult();
        RichSql richSql = sqlGenerator.generate(tableConfig, pagination, extSqlCondition);
        if (richSql == null) {
            return scanPageResult;
        }
        List<Row> rows = magicDb.getDbPool().executeForList(richSql);
        Pagination nextPagination = sqlGenerator.getNextPagination(pagination, rows);
        scanPageResult.setNextPagination(nextPagination);
        scanPageResult.setRows(rows);
        return scanPageResult;
    }

    private SqlGenerator getSqlGenerator(TableConfig tableConfig) throws SQLException {
        final String key = tableConfig.getDbConfig().getDbName() + "||" + tableConfig.getTableName();
        SqlGenerator sqlGenerator = tableSqlGeneratorCache.get(key);
        if (sqlGenerator != null) {
            return sqlGenerator;
        }
        Columns columns = magicDb.getColumns(tableConfig);
        Column pkColumn = columns.getPkColumn();
        if (pkColumn == null) {
            return null;
        }
        Class<?> clazz = TypeUtil.getJavaType(pkColumn.getSqlType());
        if (String.class.equals(clazz)) {
            sqlGenerator = new StringPkSqlGenerator(magicDb);
        } else {
            sqlGenerator = new LongPkSqlGenerator(magicDb);
        }
        tableSqlGeneratorCache.put(key, sqlGenerator);
        return sqlGenerator;
    }



}
