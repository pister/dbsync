package com.github.pister.dbsync.rt;


import com.github.pister.dbsync.config.Column;

import java.io.Serializable;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class FieldValue implements Serializable {

    private static final long serialVersionUID = 8087039121524896830L;
    private Column column;

    private Object value;

    public FieldValue() {
    }

    public FieldValue(Column column, Object value) {
        this.column = column;
        this.value = value;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "FieldValue{" +
                "column=" + column +
                ", value=" + value +
                '}';
    }
}
