package com.github.pister.dbsync.common.db.seq;


import com.github.pister.dbsync.common.db.sql.SqlExecutor;
import com.github.pister.dbsync.common.db.NotCloseConnection;
import com.github.pister.dbsync.common.tools.util.SqlUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: huangsongli
 * Date: 16/4/29
 * Time: 下午1:53
 */
/*
    create table sequences (
        name varchar(64) primary key not null,
        next_value bigint default 1 not null,
        last_modified datetime not null
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

 */
public class DbTableRangeLoader implements RangeLoader {

    private DataSource dataSource;
    private String GET_SQL = "select name, next_value, last_modified from sequences where name = ?";
    private String COMPARE_AND_UPDATE_SQL = "update sequences set next_value = ?, last_modified = now() where name = ? and next_value = ?";
    private String INIT_SQL = "insert into sequences(name, next_value, last_modified) values(?, 1, now())";
    private int step = 2000;

    public void init() {
        testForCheck();
    }

    private void testForCheck() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            NotCloseConnection conn = new NotCloseConnection(connection);
            getValue(conn, "test_name");
        } catch (SQLException e) {
            throw new RuntimeException("Are You Sure the TABLE sequences exist in this DataSource?\r\n"
                    + "you can create table sequence table use the script:\r\n"
                    + " create table sequences (\n" +
                    "        name varchar(64) primary key not null,\n" +
                    "        next_value bigint default 1 not null,\n" +
                    "        last_modified datetime not null\n" +
                    "    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;\r\n", e);
        } finally {
            SqlUtil.close(connection);
        }
    }

    @Override
    public IdRange loadNextRange(String name) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            NotCloseConnection conn = new NotCloseConnection(connection);
            Long value;
            long endValue;
            for (;;) {
                value = getValue(conn, name);
                if (value == null) {
                    if (!initSequence(conn, name)) {
                        continue;
                    }
                    value = 1L;
                }

                long nextValue= value + step;
                endValue = nextValue - 1;
                if (compareAndUpdate(conn, name, value, nextValue)) {
                    break;
                }
            }
            IdRange idRange = new IdRange();
            idRange.setStart(value);
            idRange.setEnd(endValue);
            return idRange;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SqlUtil.close(connection);
        }
    }

    private boolean initSequence(NotCloseConnection conn, String name) {
        try {
            if (SqlExecutor.executeUpdate(conn, INIT_SQL, name) > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            // 执行错误，例如同一name记录已经存在等。
            return false;
        }
    }

    private boolean compareAndUpdate(NotCloseConnection conn, String name, long oldValue, long newValue) {
        return SqlExecutor.executeUpdate(conn, COMPARE_AND_UPDATE_SQL, newValue, name, oldValue) > 0;
    }

    private Long getValue(NotCloseConnection conn, String name) {
        return SqlExecutor.queryForObject(conn, rs -> rs.getLong("next_value"), GET_SQL, name);
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
