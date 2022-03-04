package com.jiudengnile.common.transfer.shard;

import java.io.Serializable;

/**
 * User: huangsongli
 * Date: 16/4/27
 * Time: 下午8:07
 */
public class ShardInfo implements Serializable {

    private static final long serialVersionUID = 5650154641461190483L;

    protected int databaseIndex;

    protected int tableIndex;

    public int getDatabaseIndex() {
        return databaseIndex;
    }

    public void setDatabaseIndex(int databaseIndex) {
        this.databaseIndex = databaseIndex;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShardInfo)) return false;

        ShardInfo shardInfo = (ShardInfo) o;

        if (databaseIndex != shardInfo.databaseIndex) return false;
        if (tableIndex != shardInfo.tableIndex) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = databaseIndex;
        result = 31 * result + tableIndex;
        return result;
    }

    @Override
    public String toString() {
        return "ShardInfo{" +
                "databaseIndex=" + databaseIndex +
                ", tableIndex=" + tableIndex +
                '}';
    }
}
