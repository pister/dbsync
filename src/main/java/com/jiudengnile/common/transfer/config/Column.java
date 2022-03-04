package com.jiudengnile.common.transfer.config;

import java.io.Serializable;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class Column implements Serializable{

    private static final long serialVersionUID = -8295405400941660L;
    private String name;

    private int sqlType;

    public Column(String name, int sqlType) {
        this.name = name;
        this.sqlType = sqlType;
    }

    public Column() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        if (sqlType != column.sqlType) return false;
        return name != null ? name.equals(column.name) : column.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + sqlType;
        return result;
    }

    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", sqlType=" + sqlType +
                '}';
    }
}
