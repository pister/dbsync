package com.github.pister.dbsync.config.mapping;

import com.github.pister.dbsync.rt.Row;
import com.github.pister.dbsync.shard.ShardInfo;
import com.github.pister.dbsync.base.DbTable;

import java.util.List;

/**
 * Created by songlihuang on 2021/1/23.
 */
public interface RouteTableMapping {

    List<DbTable> getTableNames();

    ShardInfo route(Row row);

}
