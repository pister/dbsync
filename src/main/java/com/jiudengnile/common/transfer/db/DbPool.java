package com.jiudengnile.common.transfer.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.jiudengnile.common.transfer.config.Column;
import com.jiudengnile.common.transfer.config.DbConfig;
import com.jiudengnile.common.transfer.rt.FieldValue;
import com.jiudengnile.common.transfer.rt.RichSql;
import com.jiudengnile.common.transfer.rt.Row;
import com.jiudengnile.common.transfer.scan.ExecuteCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wint.help.sql.*;
import wint.lang.utils.CollectionUtil;
import wint.lang.utils.MapUtil;

import javax.sql.DataSource;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class DbPool {

    private static final Logger log = LoggerFactory.getLogger(DbPool.class);

    private ConcurrentMap<DbConfig, DataSource> dataSourceMap = MapUtil.newConcurrentHashMap();

    public List<Row> executeForList(RichSql richSql) throws SQLException {
        ScanRowProcessor scanRowProcessor = new ScanRowProcessor(richSql.getColumns(), richSql.getPkName());
        Connection connection = getConnection(richSql.getDbConfig());
        if (log.isDebugEnabled()) {
            log.debug("execute sql:" + richSql.getSql() + " with params:" + Arrays.toString(richSql.getParams()));
        }
        SqlExecutor.executeQuery(connection, scanRowProcessor, richSql.getSql(), richSql.getParams());
        return scanRowProcessor.getRows();
    }

    public <T> List<T> executeForList(DbConfig dbConfig, RowProcessor<T> rowProcessor, String sql, Object... args) throws SQLException {
        List<T> ret = CollectionUtil.newArrayList();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = getConnection(dbConfig);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setFetchSize(1);
            Binder.bindParameters(pstmt, args);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                T t = rowProcessor.processRow(rs);
                ret.add(t);
            }
        } finally {
            SqlUtil.close(rs);
            SqlUtil.close(pstmt);
            SqlUtil.close(conn);
        }
        return ret;
    }

    public <T> T executeForObject(DbConfig dbConfig, RowProcessor<T> rowProcessor, String sql, Object... args) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = getConnection(dbConfig);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setFetchSize(1);
            Binder.bindParameters(pstmt, args);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowProcessor.processRow(rs);
            }
        } finally {
            SqlUtil.close(rs);
            SqlUtil.close(pstmt);
            SqlUtil.close(conn);
        }
        return null;
    }

    public <T> int executeQuery(DbConfig dbConfig, RowProcessor<T> rowProcessor, String sql, Object... args) throws SQLException {
        int count = 0;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = getConnection(dbConfig);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setFetchSize(1);
            Binder.bindParameters(pstmt, args);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                rowProcessor.processRow(rs);
                ++count;
            }
        } finally {
            SqlUtil.close(rs);
            SqlUtil.close(pstmt);
            SqlUtil.close(conn);
        }
        return count;
    }

    public Object execute(DbConfig dbConfig, ExecuteCallback executeCallback) throws SQLException {
        Connection connection = getConnection(dbConfig);
        try {
            return executeCallback.executeOnConnection(connection);
        } finally {
            SqlUtil.close(connection);
        }
    }

    public int executeUpdate(DbConfig dbConfig, String sql, Object... args) throws SQLException {
        int ret;
        PreparedStatement pstmt = null;
        Connection conn = getConnection(dbConfig);
        try {
            pstmt = conn.prepareStatement(sql);
            Binder.bindParameters(pstmt, args);
            ret = pstmt.executeUpdate();
        } finally {
            SqlUtil.close(pstmt);
            SqlUtil.close(conn);
        }
        return ret;
    }

    public static class ScanRowProcessor implements RowProcessor<Row> {

        private List<Column> fields;

        private List<Row> rows = CollectionUtil.newArrayList();

        private String pkName;

        public ScanRowProcessor(List<Column> fields, String pkName) {
            this.fields = fields;
            this.pkName = pkName;
        }

        @Override
        public Row processRow(ResultSet rs) throws SQLException {
            Row row = new Row();
            row.setPkName(pkName);
            for (Column column : fields) {
                String fieldName = column.getName();
                int sqlType = column.getSqlType();
                Class<?> clazz = TypeUtil.getJavaType(sqlType);
                if (clazz == null) {
                    clazz = String.class;
                }
                TypeUtil.MethodPair methodPair = TypeUtil.getMethodPair(clazz);
                Method method = methodPair.getGetter();
                try {
                    Object value = method.invoke(rs, fieldName);
                    FieldValue fieldValue = new FieldValue();
                    fieldValue.setColumn(column);
                    fieldValue.setValue(value);
                    row.addField(fieldName, fieldValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e.getTargetException());
                }
            }
            rows.add(row);
            return row;
        }

        public List<Row> getRows() {
            return rows;
        }
    }

    private Connection getConnection(DbConfig dbConfig) throws SQLException {
        DataSource dataSource = getDateSource(dbConfig);
        return dataSource.getConnection();
    }

    public DataSource getDateSource(DbConfig dbConfig) throws SQLException {
        DataSource dataSource = dataSourceMap.get(dbConfig);
        if (dataSource != null) {
            return dataSource;
        }
        DruidDataSource newDataSource = initDataSource(dbConfig);
        DataSource existDataSource = dataSourceMap.putIfAbsent(dbConfig, newDataSource);
        if (existDataSource != null) {
            close(newDataSource);
            return existDataSource;
        }
        return newDataSource;
    }

    private static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }


    private DruidDataSource initDataSource(DbConfig dbConfig) throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUsername(dbConfig.getUsername());
        druidDataSource.setPassword(dbConfig.getPassword());
        druidDataSource.setUrl(dbConfig.getUrl());
        druidDataSource.setFilters("stat");
        druidDataSource.setMaxActive(200);
        druidDataSource.setInitialSize(1);
        druidDataSource.setMaxWait(60000);
        druidDataSource.setMinIdle(1);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);
        druidDataSource.setValidationQuery("SELECT 'x'");
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setPoolPreparedStatements(true);
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(50);
        druidDataSource.init();
        return druidDataSource;
    }

}
