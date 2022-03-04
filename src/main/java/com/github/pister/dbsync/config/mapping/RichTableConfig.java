package com.github.pister.dbsync.config.mapping;

import com.github.pister.dbsync.util.CollectionUtil;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.config.TableConfig;
import com.github.pister.dbsync.util.MapUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2021/3/11.
 */
public class RichTableConfig {

    /**
     * 目标表名，分表的时候为表名模板，类似: abc_%04d
     */
    private String tableName;

    /**
     * 不分表时候的数据索引
     */
    private int singleTableDbIndex;

    /**
     * 本地库是否支持分库分表，
     * 如果支持:
     * 1、routeColumn, dbCount和tableCountPerDb为必填
     * 2、localTable字段为表名模板，后面会类似 String.format(tableName, index) 生成真实表名
     */
    private boolean shardSupport;

    /**
     * 分表字段，按此字段分表，计算规则参考：RouteUtil.getLongValue()
     */
    private String routeColumn;

    /**
     * 数据库起始索引位置偏移
     */
    private int dbIndexOffset = 0;

    /**
     * 目标数据库数量
     */
    private int dbCount;

    /**
     * 每个库的表数量
     */
    private int tableCountPerDb;



    public static RichTableConfig makeSingle(int dbIndex, String tableName) {
        RichTableConfig tableTaskConfig = new RichTableConfig();
        tableTaskConfig.setShardSupport(false);
        tableTaskConfig.setSingleTableDbIndex(dbIndex);
        tableTaskConfig.setTableName(tableName);
        return tableTaskConfig;
    }

    public static RichTableConfig makeOneTooManyShard(String tableFormat,
                                                      String routeColumn, int dbCount, int tableCountPerDb, int dbIndexOffset) {
        RichTableConfig tableTaskConfig = new RichTableConfig();
        tableTaskConfig.setShardSupport(true);
        tableTaskConfig.setTableName(tableFormat);
        tableTaskConfig.setRouteColumn(routeColumn);
        tableTaskConfig.setDbIndexOffset(dbIndexOffset);
        tableTaskConfig.setDbCount(dbCount);
        tableTaskConfig.setTableCountPerDb(tableCountPerDb);
        return tableTaskConfig;
    }



    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    protected String formatShardTableName(String localTable, int tableIndex) {
        return String.format(localTable, tableIndex);
    }

    public Map<Integer, List<TableConfig>> getShardTables(Map<Integer, DbConfig> indexedDbConfigs) {
        if (!shardSupport) {
            throw new UnsupportedOperationException("not support.");
        }
        Map<Integer, List<TableConfig>> ret = MapUtil.newHashMap();
        int tableIndex = 0;
        for (int dbIndex = 0; dbIndex < dbCount; dbIndex++) {
            DbConfig dbConfig = indexedDbConfigs.get(dbIndex);
            List<TableConfig> tableConfigList = CollectionUtil.newArrayList(tableCountPerDb);
            for (int i = 0; i < tableCountPerDb; i++) {
                TableConfig tableConfig = new TableConfig();
                tableConfig.setDbConfig(dbConfig);
                tableConfig.setTableName(formatShardTableName(tableName, tableIndex));
                tableConfigList.add(tableConfig);
                tableIndex++;
            }
            ret.put(dbIndex, tableConfigList);
        }
        return ret;
    }

    public boolean isShardSupport() {
        return shardSupport;
    }

    public void setShardSupport(boolean shardSupport) {
        this.shardSupport = shardSupport;
    }

    public int getDbCount() {
        return dbCount;
    }

    public void setDbCount(int dbCount) {
        this.dbCount = dbCount;
    }

    public int getTableCountPerDb() {
        return tableCountPerDb;
    }

    public void setTableCountPerDb(int tableCountPerDb) {
        this.tableCountPerDb = tableCountPerDb;
    }

    public String getRouteColumn() {
        return routeColumn;
    }

    public void setRouteColumn(String routeColumn) {
        this.routeColumn = routeColumn;
    }

    public int getSingleTableDbIndex() {
        return singleTableDbIndex;
    }

    public void setSingleTableDbIndex(int singleTableDbIndex) {
        this.singleTableDbIndex = singleTableDbIndex;
    }

    public int getDbIndexOffset() {
        return dbIndexOffset;
    }

    public void setDbIndexOffset(int dbIndexOffset) {
        this.dbIndexOffset = dbIndexOffset;
    }
}
