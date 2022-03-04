package com.github.pister.dbsync.common.db.seq;


import java.util.concurrent.atomic.AtomicLong;

/**
 * User: huangsongli
 * Date: 16/4/29
 * Time: 上午11:18
 */
public class LocalRangeSequence implements Sequence {

    private AtomicLong currentValue = new AtomicLong(0);
    private AtomicLong endValue = new AtomicLong(0);
    private RangeLoader rangeLoader;
    private String name;

    public LocalRangeSequence(RangeLoader rangeLoader, String name) {
        this.rangeLoader = rangeLoader;
        this.name = name;
    }

    @Override
    public long nextValue() {
        long value = currentValue.getAndIncrement();
        while (true) {
            if (value < endValue.get()) {
                return value;
            }
            tryLoadNextRange();
            value = currentValue.getAndIncrement();
        }
    }

    private synchronized void tryLoadNextRange() {
        long value = currentValue.get();
        long end = endValue.get();
        if (value < end) {
            return;
        }
        IdRange idRange = rangeLoader.loadNextRange(name);
        currentValue.set(idRange.getStart());
        endValue.set(idRange.getEnd());
    }

}
