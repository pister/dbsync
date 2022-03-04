package com.github.pister.dbsync.common.tools.util;

/**
 * Created by songlihuang on 2021/3/12.
 */
public interface MysqlSavedRetConstants {

    /**
     * 已插入
     */
    int INSERTED = 1;

    /**
     * 执行on duplicate key部分，但是没有更新记录（比如更新字段值和原来一致）
     */
    int ON_DUPLICATE_KEY_NOT_UPDATE_FIELDS = 2;

    /**
     * 执行on duplicate key部分，并且更新了数据
     */
    int ON_DUPLICATE_KEY_HAS_UPDATED_FIELDS = 3;

}
