package com.jiudengnile.common.transfer.base;

import com.jiudengnile.common.transfer.config.DbConfig;
import com.jiudengnile.common.transfer.config.TableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wint.lang.utils.MapUtil;

import java.util.Map;

/**
 * Created by songlihuang on 2017/7/13.
 */
public abstract class AbstractPoint {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected Map<DbTable, String> lastModifiedFieldMap = MapUtil.newHashMap();

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
