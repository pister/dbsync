package com.github.pister.dbsync.runtime.aop;

import com.github.pister.dbsync.common.db.seq.Sequence;
import com.github.pister.dbsync.runtime.sync.DestProcessor;
import com.github.pister.dbsync.runtime.sync.QueryProcessor;

/**
 * Created by songlihuang on 2021/2/19.
 */
public interface AopContext {

    DestProcessor getLocalRowsProcessor();

    Sequence getSequence(String name);

}
