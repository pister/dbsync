package com.jiudengnile.common.transfer.aop;

import com.jiudengnile.common.transfer.id.Sequence;
import com.jiudengnile.common.transfer.sync.DestProcessor;
import com.jiudengnile.common.transfer.sync.QueryProcessor;

/**
 * Created by songlihuang on 2021/2/19.
 */
public interface AopContext {

    DestProcessor getLocalRowsProcessor();

    Sequence getSequence(String name);

    QueryProcessor getSourceQueryProcessor();

}
