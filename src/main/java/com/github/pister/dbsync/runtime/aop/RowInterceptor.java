package com.github.pister.dbsync.runtime.aop;

import com.github.pister.dbsync.runtime.exec.ResultRow;
import com.github.pister.dbsync.runtime.exec.Row;

import java.sql.SQLException;

/**
 * Created by songlihuang on 2021/2/19.
 */
public interface RowInterceptor {

    /**
     * 数据插入前的处理
     * @param input 即将同步的数据，你可以在实现方法中修改这些数据
     * @param aopContext
     * @return
     * @throws SQLException
     */
    RowInterceptorResult onBefore(Row input, AopContext aopContext) throws SQLException;

    /**
     * 数据同步后的处理
     * @param row 刚刚同步的数据
     * @param aopContext
     * @throws SQLException
     */
    void onAfter(ResultRow row, AopContext aopContext) throws SQLException;

}
