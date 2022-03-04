package com.jiudengnile.common.transfer.config.mapping;

import com.jiudengnile.common.transfer.config.mapping.table.MappedTable;
import com.jiudengnile.common.transfer.config.mapping.table.ShardMappedTable;
import com.jiudengnile.common.transfer.config.mapping.table.SingleMappedTable;

/**
 * Created by songlihuang on 2021/3/11.
 */
public class MappedTableUtils {

    public static MappedTable makeMappedTable(RichTableConfig richTableConfig) {
        MappedTable mappedTable;
        if (richTableConfig.isShardSupport()) {
            mappedTable = new ShardMappedTable(richTableConfig.getRouteColumn(), richTableConfig.getDbCount(),
                    richTableConfig.getTableCountPerDb(), richTableConfig.getDbIndexOffset());
        } else {
            SingleMappedTable singleMappedTable = new SingleMappedTable();
            singleMappedTable.setDbIndex(richTableConfig.getSingleTableDbIndex());
            mappedTable = singleMappedTable;
        }
        mappedTable.setLocalTable(richTableConfig.getTableName());
        mappedTable.setUpdatedField(richTableConfig.getTableName());
        return mappedTable;
    }

}
