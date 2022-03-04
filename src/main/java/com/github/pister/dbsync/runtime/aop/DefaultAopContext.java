package com.github.pister.dbsync.runtime.aop;

import com.github.pister.dbsync.common.db.seq.LocalSequence;
import com.github.pister.dbsync.runtime.sync.DestProcessor;
import com.github.pister.dbsync.common.db.seq.Sequence;
import com.github.pister.dbsync.runtime.sync.QueryProcessor;

/**
 * Created by songlihuang on 2021/2/19.
 */
public class DefaultAopContext implements AopContext {

    private DestProcessor destProcessor;

    private LocalSequence localSequence;

    private QueryProcessor sourceQueryProcessor;

    public DefaultAopContext(QueryProcessor sourceQueryProcessor, DestProcessor destProcessor, LocalSequence localSequence) {
        this.sourceQueryProcessor = sourceQueryProcessor;
        this.destProcessor = destProcessor;
        this.localSequence = localSequence;
    }

    @Override
    public DestProcessor getLocalRowsProcessor() {
        return destProcessor;
    }

    @Override
    public Sequence getSequence(String name) {
        if (localSequence == null) {
            throw new IllegalArgumentException("db sequence index is not set!");
        }
        return localSequence.getSequence(name);
    }

    @Override
    public QueryProcessor getSourceQueryProcessor() {
        return sourceQueryProcessor;
    }


}
