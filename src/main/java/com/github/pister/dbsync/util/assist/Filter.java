package com.github.pister.dbsync.util.assist;

public interface Filter<T> {

    boolean accept(T t);

}