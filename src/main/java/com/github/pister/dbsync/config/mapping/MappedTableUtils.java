package com.github.pister.dbsync.config.mapping;

import com.github.pister.dbsync.config.mapping.table.MappedTable;
import com.github.pister.dbsync.config.mapping.table.ShardMappedTable;
import com.github.pister.dbsync.config.mapping.table.SingleMappedTable;

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
