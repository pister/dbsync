package com.github.pister.dbsync.common.db.shard;

/**
 * Created by songlihuang on 2022/3/5.
 */
public class DefaultShardStrategy implements ShardStrategy {
    @Override
    public long routing(Object value) {
        return RouteUtil.getLongValue(value);
    }
}
