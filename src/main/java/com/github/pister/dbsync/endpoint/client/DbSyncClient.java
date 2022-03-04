package com.github.pister.dbsync.endpoint.client;

import com.github.pister.dbsync.config.mapping.table.MappedTable;
import com.github.pister.dbsync.common.db.DbPool;
import com.github.pister.dbsync.common.db.seq.LocalSequence;
import com.github.pister.dbsync.common.db.MagicDb;
import com.github.pister.dbsync.endpoint.server.DbSyncServer;
import com.github.pister.dbsync.runtime.sync.ProcessListener;
import com.github.pister.dbsync.common.tools.util.CollectionUtil;
import com.github.pister.dbsync.endpoint.base.AbstractPoint;
import com.github.pister.dbsync.runtime.sync.Saver;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.config.mapping.MappedTableUtils;
import com.github.pister.dbsync.config.mapping.TableTaskConfig;
import com.github.pister.dbsync.runtime.progress.FileProgressManager;
import com.github.pister.dbsync.runtime.progress.ProgressManager;
import com.github.pister.dbsync.runtime.sync.MappedDestProcessor;
import com.github.pister.dbsync.runtime.sync.TableSyncProcessor;
import com.github.pister.dbsync.common.tools.util.MapUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by songlihuang on 2021/1/24.
 */
public class DbSyncClient extends AbstractPoint {

    private Map<Integer, DbConfig> localDbConfigs = MapUtil.newHashMap();

    private List<TableTaskConfig> tableTaskConfigList = CollectionUtil.newArrayList();

    private Map<String, MappedTable> /* taskName => MappedTable */ tables = MapUtil.newHashMap();

    private DbSyncServer dbSyncServer;

    private MagicDb magicDb;

    private Saver saver;

    private ProgressManager progressManager;

    private TableSyncProcessor tableSyncProcessor;

    private LocalSequence localSequence;

    private Integer sequenceDbIndex;

    private boolean ignoreCheck = false;


    private static final ProcessListener EMPTY_PROCESS_LISTENER = new ProcessListener() {

        @Override
        public void onProcess(String taskName, int rows) {

        }

        @Override
        public void onFinish(String taskName) {

        }
    };


    public void addTableTaskConfig(TableTaskConfig tableTaskConfig) {
        tableTaskConfigList.add(tableTaskConfig);
    }

    /**
     * 通过索引注册数据库相关配置，
     * 后面用的时候采用索引获取，
     * 注册同一索引会覆盖之前的配置
     *
     * @param index
     * @param dbConfig
     */
    public void registerLocalDb(int index, DbConfig dbConfig) {
        localDbConfigs.put(index, dbConfig);
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

        tableSyncProcessor = new TableSyncProcessor(dbSyncServer,
                new MappedDestProcessor(localDbConfigs, saver, magicDb, dbPool),
                progressManager, tables);

        log.warn("DbSyncClient init success.");
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
            mappedTable.check(dbSyncServer, magicDb, localDbConfigs);
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

    public int exec(String taskName, ProcessListener processListener) throws Exception {
        return tableSyncProcessor.syncTable(taskName, processListener, localSequence);
    }

    public int exec(String taskName) throws Exception {
        return exec(taskName, EMPTY_PROCESS_LISTENER);
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

    public void setDbSyncServer(DbSyncServer dbSyncServer) {
        this.dbSyncServer = dbSyncServer;
    }
}