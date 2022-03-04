package com.jiudengnile.common.transfer.aop;

import com.jiudengnile.common.transfer.rt.ResultRow;
import com.jiudengnile.common.transfer.rt.Row;

import java.sql.SQLException;
import java.util.List;

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
