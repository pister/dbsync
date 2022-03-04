package com.jiudengnile.common.transfer.sync;

/**
 * Created by songlihuang on 2021/2/19.
 */
public interface ProcessListener {

    void onProcess(String taskName, int rows);

    void onFinish(String taskName);

}
