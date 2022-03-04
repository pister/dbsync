package com.github.pister.dbsync.runtime.progress;

import java.io.IOException;

/**
 * Created by songlihuang on 2017/7/13.
 */
public interface ProgressManager {

    void save(String taskName, TableProgress tableProgress) throws IOException;

    TableProgress load(String taskName) throws IOException;

}
