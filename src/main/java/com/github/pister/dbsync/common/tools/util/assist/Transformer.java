package com.github.pister.dbsync.common.tools.util.assist;

public interface Transformer<S, T> {

    T transform(S object);

}