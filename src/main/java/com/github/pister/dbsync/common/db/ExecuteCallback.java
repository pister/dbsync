package com.github.pister.dbsync.common.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by songlihuang on 2017/7/11.
 */
public interface ExecuteCallback {

    Object executeOnConnection(Connection connection) throws SQLException;
}
