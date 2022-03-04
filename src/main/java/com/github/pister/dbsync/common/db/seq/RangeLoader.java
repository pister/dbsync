package com.github.pister.dbsync.common.db.seq;

/**
 * User: huangsongli
 * Date: 16/4/29
 * Time: 上午11:40
 */
public interface RangeLoader {

    IdRange loadNextRange(String name);

}
