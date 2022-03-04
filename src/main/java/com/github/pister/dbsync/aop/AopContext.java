package com.github.pister.dbsync.aop;

import com.github.pister.dbsync.id.Sequence;
import com.github.pister.dbsync.sync.DestProcessor;
import com.github.pister.dbsync.sync.QueryProcessor;

/**
 * Created by songlihuang on 2021/2/19.
 */
public interface AopContext {

    DestProcessor getLocalRowsProcessor();

    Sequence getSequence(String name);

    QueryProcessor getSourceQueryProcessor();

}
