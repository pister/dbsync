package com.github.pister.dbsync.common.tools.util.assist;

public interface Filter<T> {

    boolean accept(T t);

}