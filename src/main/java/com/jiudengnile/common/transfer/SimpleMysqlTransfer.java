package com.jiudengnile.common.transfer;

import com.jiudengnile.common.transfer.config.DbConfig;
import com.jiudengnile.common.transfer.config.mapping.TableTaskConfig;

import java.sql.SQLException;

/**
 * mysql的数据同步工具，支持如下功能：
 * 1、全量迁移，增量迁移
 * 2、表名映射，字段名映射（目标字段可以少于源字段）
 * 3、支持分库分布
 * 4、支持字段修改，字段拦截，记录过滤和多表合并
 * <p>
 * Created by songlihuang on 2021/2/19.
 */
public class SimpleMysqlTransfer extends MysqlTransfer {

    private DefaultTransferServer transferServer = new DefaultTransferServer();

    private TableTransferClient tableTransferClient = new TableTransferClient();

    private boolean ignoreCheck = false;


    /**
     * 请注意顺序，使用的过程要按0,1,2,3...序列获取
     *
     * @param dbConfig
     */
    public void addSourceDbConfig(DbConfig dbConfig) {
        transferServer.addDbConfig(dbConfig);
    }

    /**
     * 这里必须严格按照先后顺序设置，
     * 0 第一个db
     * 1 第二个db
     * 3 第三个db
     * ...
     *
     * @param dbConfig
     */
    public void addDestDbConfig(DbConfig dbConfig) {
        tableTransferClient.addLocalDb(dbConfig);
    }

    /**
     * 增加数据同步任务
     *
     * @param tableTaskConfig
     */
    public void addTask(TableTaskConfig tableTaskConfig) {
        tableTransferClient.addTableTaskConfig(tableTaskConfig);
    }

    /**
     * 设置目标数据的序列所在数据库
     * @param index
     */
    public void setDestSeqDbIndex(int index) {
        tableTransferClient.setSequenceDbIndex(index);
    }

    @Override
    protected TransferServer initTransferServer() throws SQLException {
        transferServer.init();
        return transferServer;
    }

    @Override
    protected TableTransferClient initTableTransferClient(TransferServer source) throws Exception {
        tableTransferClient.setTransferServer(source);
        tableTransferClient.setIgnoreCheck(ignoreCheck);
        tableTransferClient.init();
        return tableTransferClient;
    }

    public void setIgnoreCheck(boolean ignoreCheck) {
        this.ignoreCheck = ignoreCheck;
    }
}
