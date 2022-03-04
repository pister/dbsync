package com.github.pister.dbsync.common.db.sql;


import com.github.pister.dbsync.runtime.exec.RowProcessor;
import com.github.pister.dbsync.common.tools.util.SqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author pister
 *         2012-9-11 下午3:10:09
 */
public class SqlExecutor {

    public static int executeUpdate(Connection conn, String sql, Object... args) {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            Binder.bindParameters(pstmt, args);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SqlUtil.close(pstmt);
            SqlUtil.close(conn);
        }
    }

    public static <T> int executeQuery(Connection conn, RowProcessor<T> rowProcessor, String sql, Object... args) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setFetchSize(1);
            Binder.bindParameters(pstmt, args);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                rowProcessor.processRow(rs);
                ++count;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SqlUtil.close(rs);
            SqlUtil.close(pstmt);
            SqlUtil.close(conn);
        }
        return count;
    }

    public static <T> T queryForObject(Connection conn, RowProcessor<T> rowProcessor, String sql, Object... args) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            Binder.bindParameters(pstmt, args);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowProcessor.processRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SqlUtil.close(rs);
            SqlUtil.close(pstmt);
            SqlUtil.close(conn);
        }
    }
}
