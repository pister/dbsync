package com.github.pister.dbsync.runtime.aop;

import com.github.pister.dbsync.common.db.seq.LocalSequence;
import com.github.pister.dbsync.common.db.seq.Sequence;
import com.github.pister.dbsync.runtime.sync.DestProcessor;

/**
 * Created by songlihuang on 2021/2/19.
 */
public class DefaultAopContext implements AopContext {

    private DestProcessor destProcessor;

    private LocalSequence localSequence;

    public DefaultAopContext(DestProcessor destProcessor, LocalSequence localSequence) {
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


}
