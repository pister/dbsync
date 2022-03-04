package com.github.pister.dbsync.common.db.shard;

/**
 * Created by songlihuang on 2022/3/5.
 */
public interface ShardStrategy {

    long routing(Object value);
}
