package com.github.pister.dbsync.util.assist;

public interface Transformer<S, T> {

    T transform(S object);

}