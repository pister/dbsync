package com.jiudengnile.common.transfer.config.mapping;

import com.jiudengnile.common.transfer.base.DbTable;
import com.jiudengnile.common.transfer.rt.Row;
import com.jiudengnile.common.transfer.shard.ShardInfo;

import java.util.List;

/**
 * Created by songlihuang on 2021/1/23.
 */
public interface RouteTableMapping {

    List<DbTable> getTableNames();

    ShardInfo route(Row row);

}
