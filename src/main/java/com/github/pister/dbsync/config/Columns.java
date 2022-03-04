package com.github.pister.dbsync.config;


import com.github.pister.dbsync.util.CollectionUtil;
import com.github.pister.dbsync.util.StringUtil;

import java.io.Serializable;
import java.util.List;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class Columns implements Serializable {

    private static final long serialVersionUID = 2074881510857975329L;
    private List<Column> columns;

    private String pkName;

    public Column getPkColumn() {
        if (CollectionUtil.isEmpty(columns) || StringUtil.isEmpty(pkName)) {
            return null;
        }
        for (Column column : columns) {
            if (StringUtil.equals(pkName, column.getName())) {
                return column;
            }
        }
        return null;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    @Override
    public String toString() {
        return "Columns{" +
                "columns=" + columns +
                ", pkName=" + pkName +
                '}';
    }
}
