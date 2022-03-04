package com.jiudengnile.common.transfer.db;

import com.jiudengnile.common.transfer.id.Sequence;
import com.jiudengnile.common.transfer.id.seq.DbTableRangeLoader;
import com.jiudengnile.common.transfer.id.seq.LocalRangeSequence;
import wint.lang.utils.MapUtil;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by songlihuang on 2021/3/11.
 */
public class LocalSequence {

    private DataSource dataSource;

    private Integer idRangeStep;

    private DbTableRangeLoader dbTableRangeLoader;

    private ConcurrentMap<String, Sequence> namedSequences = MapUtil.newConcurrentHashMap();

    public LocalSequence(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void init() {
        DbTableRangeLoader dbTableRangeLoader = new DbTableRangeLoader();
        dbTableRangeLoader.setDataSource(dataSource);
        if (idRangeStep != null && idRangeStep > 0) {
            dbTableRangeLoader.setStep(idRangeStep);
        }
        dbTableRangeLoader.init();
        this.dbTableRangeLoader = dbTableRangeLoader;
    }

    public void setIdRangeStep(Integer idRangeStep) {
        this.idRangeStep = idRangeStep;
    }

    public Sequence getSequence(String name) {
        Sequence sequence = namedSequences.get(name);
        if (sequence != null) {
            return sequence;
        }
        LocalRangeSequence newSequence = new LocalRangeSequence(dbTableRangeLoader, name);
        sequence = namedSequences.putIfAbsent(name, newSequence);
        if (sequence != null) {
            return sequence;
        }
        return newSequence;
    }


}
