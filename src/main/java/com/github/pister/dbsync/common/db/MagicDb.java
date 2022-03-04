package com.github.pister.dbsync.common.db;

import com.github.pister.dbsync.common.tools.util.CollectionUtil;
import com.github.pister.dbsync.common.cache.JvmCache;
import com.github.pister.dbsync.config.Column;
import com.github.pister.dbsync.config.Columns;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.config.TableConfig;
import com.github.pister.dbsync.common.tools.util.SqlUtil;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class MagicDb {

    private DbPool dbPool;

    private JvmCache columnsCache = new JvmCache();

    public MagicDb(DbPool dbPool) {
        this.dbPool = dbPool;
    }

    public List<String> listTables(DbConfig dbConfig) throws SQLException {
        return (List<String>) dbPool.execute(dbConfig, connection -> {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            List<String> tables = CollectionUtil.newArrayList();
            ResultSet rs = databaseMetaData.getTables(null, null, null, null);
            try {
                while (rs.next()) {
                    String name = rs.getString("TABLE_NAME");
                    tables.add(name);
                }
            } finally {
                SqlUtil.close(rs);
            }
            return tables;
        });
    }

    public Columns getColumns(final TableConfig tableConfig) throws SQLException {
        final String key = tableConfig.getDbConfig().getDbName() + "." + tableConfig.getTableName();
        Columns columns = (Columns) columnsCache.get(key);
        if (columns != null) {
            return columns;
        }
        columns = getColumnsImpl(tableConfig);
        columnsCache.set(key, columns, 3600);
        return columns;
    }

    private Columns getColumnsImpl(final TableConfig tableConfig) throws SQLException {
        final Columns columns = new Columns();
        dbPool.execute(tableConfig.getDbConfig(), connection -> {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            {
                ResultSet rs = databaseMetaData.getColumns(null, null, tableConfig.getTableName(), null);
                fillColumns(rs, columns);
            }

            {
                ResultSet rs = databaseMetaData.getPrimaryKeys(null, null, tableConfig.getTableName());
                fillPk(rs, columns);
            }

            return null;
        });
        return columns;
    }

    private void fillPk(ResultSet rs, Columns columns) throws SQLException {
        try {
            if (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                columns.setPkName(name);
            }
        } finally {
            SqlUtil.close(rs);
        }
    }

    private void fillColumns(ResultSet rs, Columns columns) throws SQLException {
        try {
            List<Column> columnList = CollectionUtil.newArrayList();
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                int dataType = rs.getInt("DATA_TYPE");
                Column column = new Column();
                column.setName(name);
                column.setSqlType(dataType);
                columnList.add(column);
            }
            columns.setColumns(columnList);
        } finally {
            SqlUtil.close(rs);
        }
    }

    public DbPool getDbPool() {
        return dbPool;
    }
}
