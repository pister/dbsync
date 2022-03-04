package com.github.pister.dbsync;

import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.common.db.DbPool;
import com.github.pister.dbsync.common.tools.util.MySqlUtil;
import junit.framework.TestCase;

import java.sql.SQLException;

/**
 * Created by songlihuang on 2021/3/12.
 */
public class ExecutorTests extends TestCase {

    public void test0() throws SQLException {
        DbPool dbPool = new DbPool();
        DbConfig dbConfig = MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_00", "trans_test_user", "trans_test_pwd");
        String sql = "insert into trans_test_abc_0000(id, gmt_modified, length_in_time, gmt_create, name, uniq_key) values(24, now(), 2, now(), 'xx', 'key123')" +
                " on duplicate key update length_in_time= 4, name = 'key444'";
        int value = dbPool.executeUpdate(dbConfig, sql);
        System.out.println(value);
        // 1 insert
        // 2 on duplicate key update effective rows 0
        // 3 on duplicate key update effective rows 1
    }
}
