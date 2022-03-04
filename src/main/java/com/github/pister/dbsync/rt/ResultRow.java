package com.github.pister.dbsync.rt;

import com.github.pister.dbsync.util.MysqlSavedRetConstants;

/**
 * Created by songlihuang on 2021/3/12.
 */
public class ResultRow {


    /**
     * 刚刚插入/更新到数据库的记录
     */
    private Row row;

    /**
     * 执行插入/更新的返回值
     * 可以参考
     * @see MysqlSavedRetConstants
     */
    private int returnValue;

    public ResultRow(Row row, int returnValue) {
        this.row = row;
        this.returnValue = returnValue;
    }

    public Row getRow() {
        return row;
    }

    public int getReturnValue() {
        return returnValue;
    }

}
