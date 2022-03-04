package com.github.pister.dbsync.scan;

import com.github.pister.dbsync.config.Column;
import com.github.pister.dbsync.rt.Row;
import com.github.pister.dbsync.config.Columns;
import com.github.pister.dbsync.config.TableConfig;
import com.github.pister.dbsync.rt.Pagination;
import com.github.pister.dbsync.rt.RichSql;
import com.github.pister.dbsync.rt.ScanPageResult;
import com.github.pister.dbsync.sql.TypeUtil;
import com.github.pister.dbsync.util.MapUtil;

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
