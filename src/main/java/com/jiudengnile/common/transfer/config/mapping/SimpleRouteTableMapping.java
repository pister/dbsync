package com.jiudengnile.common.transfer.config.mapping;

import com.jiudengnile.common.transfer.base.DbTable;
import com.jiudengnile.common.transfer.rt.FieldValue;
import com.jiudengnile.common.transfer.rt.Row;
import com.jiudengnile.common.transfer.shard.RouteUtil;
import com.jiudengnile.common.transfer.shard.ShardInfo;
import wint.lang.utils.CollectionUtil;

import java.util.List;

/**
 * Created by songlihuang on 2021/1/23.
 */
public class SimpleRouteTableMapping implements RouteTableMapping {

    private String destTableNameFormat = "xx_yyy_%04d";

    private String routeColumn;

    private List<String> dbNameList;

    private int tableCount; // per db

    public SimpleRouteTableMapping(List<String> dbNameList, int tableCount, String destTableNameFormat, String routeColumn) {
        this.dbNameList = dbNameList;
        this.tableCount = tableCount;
        this.destTableNameFormat = destTableNameFormat;
        this.routeColumn = routeColumn;
    }

    @Override
    public List<DbTable> getTableNames() {
        List<DbTable> ret = CollectionUtil.newArrayList(dbNameList.size() * tableCount);
        int startIndex = 0;
        for (int dbIndex = 0, len = dbNameList.size(); dbIndex < len; dbIndex++ ) {
            int end = startIndex + tableCount;
            for (int i = startIndex; i < end; i++) {
                DbTable dbTable = new DbTable(dbIndex, String.format(destTableNameFormat, i));
                ret.add(dbTable);
            }
            startIndex = end;
        }
        return ret;
    }

    @Override
    public ShardInfo route(Row row) {
        FieldValue fieldValue = row.getFields().get(routeColumn);
        final long longValue = RouteUtil.getLongValue(fieldValue.getValue());
        int totalTableCount = dbNameList.size() * tableCount;
        if (totalTableCount == 0) {
            throw new RuntimeException("totalTableCount can not be zero!");
        }
        int tableIndex = (int) (longValue % totalTableCount);
        int dbIndex = tableIndex / tableCount;

        ShardInfo shardInfo = new ShardInfo();
        shardInfo.setDatabaseIndex(dbIndex);
        shardInfo.setTableIndex(tableIndex);
        return shardInfo;
    }
}
