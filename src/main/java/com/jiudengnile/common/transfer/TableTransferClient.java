package com.jiudengnile.common.transfer;

import com.jiudengnile.common.transfer.base.AbstractPoint;
import com.jiudengnile.common.transfer.client.Saver;
import com.jiudengnile.common.transfer.config.DbConfig;
import com.jiudengnile.common.transfer.config.mapping.MappedTableUtils;
import com.jiudengnile.common.transfer.config.mapping.TableTaskConfig;
import com.jiudengnile.common.transfer.config.mapping.table.MappedTable;
import com.jiudengnile.common.transfer.db.DbPool;
import com.jiudengnile.common.transfer.db.LocalSequence;
import com.jiudengnile.common.transfer.progress.FileProgressManager;
import com.jiudengnile.common.transfer.progress.ProgressManager;
import com.jiudengnile.common.transfer.scan.MagicDb;
import com.jiudengnile.common.transfer.sync.MappedDestProcessor;
import com.jiudengnile.common.transfer.sync.ProcessListener;
import com.jiudengnile.common.transfer.sync.TableSyncProcessor;
import wint.lang.utils.CollectionUtil;
import wint.lang.utils.MapUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by songlihuang on 2021/1/24.
 */
public class TableTransferClient extends AbstractPoint {

    private List<DbConfig> localDbConfigs = CollectionUtil.newArrayList();

    private List<TableTaskConfig> tableTaskConfigList = CollectionUtil.newArrayList();

    private Map<String, MappedTable> /* taskName => MappedTable */ tables = MapUtil.newHashMap();

    private TransferServer transferServer;

    private MagicDb magicDb;

    private Saver saver;

    private ProgressManager progressManager;

    private TableSyncProcessor tableSyncProcessor;

    private LocalSequence localSequence;

    private Integer sequenceDbIndex;

    private boolean ignoreCheck = false;

    public void addTableTaskConfig(TableTaskConfig tableTaskConfig) {
        tableTaskConfigList.add(tableTaskConfig);
    }

    /**
     * 这里必须严格按照先后顺序设置，
     * 0 第一个db
     * 1 第二个db
     * 3 第三个db
     * ...
     * @param dbConfig
     */
    public void addLocalDb(DbConfig dbConfig) {
        localDbConfigs.add(dbConfig);
    }

    public void setSequenceDbIndex(int dbIndex) {
        sequenceDbIndex = dbIndex;
    }

    private void initSequence(DbPool dbPool) throws SQLException {
        log.warn("initSequence...");
        DbConfig dbConfig = localDbConfigs.get(sequenceDbIndex);
        DataSource dataSource = dbPool.getDateSource(dbConfig);
        localSequence = new LocalSequence(dataSource);
        localSequence.init();
        log.warn("initSequence finish.");

    }

    public void init() throws Exception {
        DbPool dbPool = new DbPool();
        magicDb = new MagicDb(dbPool);
        saver = new Saver(dbPool);

        if (sequenceDbIndex != null) {
            initSequence(dbPool);
        }

        loadTables();

        if (ignoreCheck) {
            log.warn("check is ignored.");
        } else {
            check();
        }


        initProgressManager();

        tableSyncProcessor = new TableSyncProcessor(transferServer,
                new MappedDestProcessor(localDbConfigs, saver, magicDb, dbPool),
                progressManager, tables);

        log.warn("TableTransferClient init success.");
    }


    private void loadTables() {
        Map<String, MappedTable> tables = MapUtil.newHashMap();
        for (TableTaskConfig tableTaskConfig : tableTaskConfigList) {
            MappedTable mappedTable = MappedTableUtils.makeMappedTable(tableTaskConfig);
            mappedTable.setBatchInterceptor(tableTaskConfig.getBatchInterceptor());
            mappedTable.setRowInterceptor(tableTaskConfig.getRowInterceptor());
            mappedTable.setRemoteDbIndex(tableTaskConfig.getRemoteDbIndex());
            mappedTable.setRemoteTable(tableTaskConfig.getRemoteTable());
            mappedTable.setOnlyFullDump(tableTaskConfig.isOnlyFullDump());
            mappedTable.setSourceExtCondition(tableTaskConfig.getSourceExtCondition());
            mappedTable.setColumnNameMapping(tableTaskConfig.getColumnNameMapping());

            if (tables.put(tableTaskConfig.getTaskName(), mappedTable) != null) {
                throw new RuntimeException("duplicate task name:" + tableTaskConfig.getTaskName());
            }
        }
        this.tables = tables;
    }


    private boolean check() throws SQLException {
        log.warn("checking...");
        for (Map.Entry<String, MappedTable> entry : tables.entrySet()) {
            MappedTable mappedTable = entry.getValue();
            mappedTable.check(transferServer, magicDb, localDbConfigs);
        }

        log.warn("check success.");
        return true;
    }

    private void initProgressManager() throws IOException {
        log.warn("init progress manager...");
        FileProgressManager fileProgressManager = new FileProgressManager();
        fileProgressManager.init();
        this.progressManager = fileProgressManager;
    }

    public int syncTable(String taskName, ProcessListener processListener) throws Exception {
        return tableSyncProcessor.syncTable(taskName, processListener, localSequence);
    }

    public boolean isIgnoreCheck() {
        return ignoreCheck;
    }

    public void setIgnoreCheck(boolean ignoreCheck) {
        this.ignoreCheck = ignoreCheck;
    }

    public Set<String> getTasks() {
        return tables.keySet();
    }

    public void setTransferServer(TransferServer transferServer) {
        this.transferServer = transferServer;
    }
}
