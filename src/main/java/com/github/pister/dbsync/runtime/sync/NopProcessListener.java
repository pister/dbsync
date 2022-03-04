package com.github.pister.dbsync.runtime.sync;

/**
 * Created by songlihuang on 2022/3/5.
 */
public class NopProcessListener implements ProcessListener {
    @Override
    public void onProcess(String taskName, int rows) {

    }

    @Override
    public void onFinish(String taskName) {

    }
}
