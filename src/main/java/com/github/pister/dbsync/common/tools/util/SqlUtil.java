package com.github.pister.dbsync.common.tools.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by songlihuang on 2022/3/4.
 */
public class SqlUtil {


    private static final Logger log = LoggerFactory.getLogger(SqlUtil.class);

    public static void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            log.error("close Connection error", e);
        }
    }

    public static void close(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (Exception e) {
            log.error("close Statement error", e);
        }
    }

    public static void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            log.error("close ResultSet error", e);
        }
    }

    public static Timestamp currentTimestamp() {
        return new Timestamp(new Date().getTime());
    }
}
