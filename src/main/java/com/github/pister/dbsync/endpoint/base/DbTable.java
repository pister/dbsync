package com.github.pister.dbsync.endpoint.base;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class DbTable {

    public int dbIndex;

    public String tableName;

    public DbTable(int dbIndex, String tableName) {
        this.dbIndex = dbIndex;
        this.tableName = tableName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        DbTable dbTable = (DbTable) object;

        if (dbIndex != dbTable.dbIndex) return false;
        return tableName != null ? tableName.equals(dbTable.tableName) : dbTable.tableName == null;
    }

    @Override
    public int hashCode() {
        int result = dbIndex;
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DbTable{" +
                "dbIndex=" + dbIndex +
                ", tableName='" + tableName + '\'' +
                '}';
    }
}
