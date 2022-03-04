package com.github.pister.dbsync.endpoint.base;

import com.github.pister.dbsync.config.TableConfig;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.common.tools.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by songlihuang on 2017/7/13.
 */
public abstract class AbstractPoint {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected Map<DbTable, String> lastModifiedFieldMap = MapUtil.newHashMap();

    protected final AtomicBoolean hasInited = new AtomicBoolean(false);

    /**
     * 设置更新时间的字段
     * @param dbIndex
     * @param tableName
     * @param lastModifiedField
     */
    public void setTableUpdatedFieldField(int dbIndex, String tableName, String lastModifiedField) {
        lastModifiedFieldMap.put(new DbTable(dbIndex, tableName), lastModifiedField);
    }

    protected TableConfig createTableConfig(String tableName, int dbIndex, DbConfig dbConfig) {
        TableConfig tableConfig = new TableConfig();
        tableConfig.setTableName(tableName);
        tableConfig.setDbConfig(dbConfig);
        String lastModifiedField = lastModifiedFieldMap.get(new DbTable(dbIndex, tableName));
        if (lastModifiedField != null) {
            tableConfig.setUpdatedField(lastModifiedField);
        }
        return tableConfig;
    }
}
