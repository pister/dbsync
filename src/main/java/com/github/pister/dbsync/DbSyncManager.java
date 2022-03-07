package com.github.pister.dbsync;

import com.github.pister.dbsync.common.tools.util.MapUtil;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.config.mapping.TableTaskConfig;
import com.github.pister.dbsync.endpoint.client.DbSyncClient;
import com.github.pister.dbsync.endpoint.server.SyncServer;
import com.github.pister.dbsync.endpoint.server.DefaultSyncServer;
import com.github.pister.dbsync.runtime.sync.NopProcessListener;
import com.github.pister.dbsync.runtime.sync.ProcessListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * mysql的数据同步工具，支持如下功能：
 * 1、全量迁移，增量迁移
 * 2、表名映射，字段名映射（目标字段可以少于源字段）
 * 3、支持分库分布
 * 4、支持字段修改，字段拦截，记录过滤和多表合并
 * <p>
 * Created by songlihuang on 2021/2/19.
 */
public class DbSyncManager {

    private static final Logger log = LoggerFactory.getLogger(DbSyncManager.class);

    private DefaultSyncServer dbSyncServer = new DefaultSyncServer();

    private DbSyncClient dbSyncClient = new DbSyncClient();

    private boolean ignoreCheck = false;

    private ExecutorService executorService;

    private AtomicBoolean inited = new AtomicBoolean(false);

    private ConcurrentMap<String, Future<Integer>> runResultMap = MapUtil.newConcurrentHashMap();

    private DbSyncClient dest;

    private ProcessListener processListener = new NopProcessListener();

    /**
     * 通过索引注册数据库相关配置，
     * 后面用的时候采用索引获取，
     * 注册同一索引会覆盖之前的配置
     *
     * @param index
     * @param dbConfig
     */
    public void registerSourceDbConfig(int index, DbConfig dbConfig) {
        dbSyncServer.registerDbConfig(index, dbConfig);
    }

    /**
     * 通过索引注册数据库相关配置，
     * 后面用的时候采用索引获取，
     * 注册同一索引会覆盖之前的配置
     *
     * @param index
     * @param dbConfig
     */
    public void registerDestDbConfig(int index, DbConfig dbConfig) {
        dbSyncClient.registerLocalDb(index, dbConfig);
    }

    /**
     * 增加数据同步任务
     *
     * @param tableTaskConfig
     */
    public void addTask(TableTaskConfig tableTaskConfig) {
        dbSyncClient.addTableTaskConfig(tableTaskConfig);
    }

    /**
     * 设置目标数据的序列所在数据库
     *
     * @param index
     */
    public void setDestSeqDbIndex(int index) {
        dbSyncClient.setSequenceDbIndex(index);
    }

    private SyncServer initTransferServer() throws SQLException {
        dbSyncServer.init();
        return dbSyncServer;
    }

    private DbSyncClient initTableTransferClient(SyncServer source) throws Exception {
        dbSyncClient.setSyncServer(source);
        dbSyncClient.setIgnoreCheck(ignoreCheck);
        dbSyncClient.init();
        return dbSyncClient;
    }

    public void setIgnoreCheck(boolean ignoreCheck) {
        this.ignoreCheck = ignoreCheck;
    }


    public void init() throws Exception {
        if (!inited.compareAndSet(false, true)) {
            throw new RuntimeException("can not call init more than once!");
        }
        SyncServer source = initTransferServer();
        DbSyncClient dest = initTableTransferClient(source);
        final int poolSize = Math.min(dest.getTasks().size(), 5);
        this.dest = dest;
        this.executorService = new ThreadPoolExecutor(poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024));
    }

    /**
     * 执行一次同步，可以通过手动调用多次
     * 对单个任务如果上次还没执行完不会同时执行
     *
     * @return true - 提交执行成功，false - 上次还没执行完
     */
    public void runOnce() {
        runOnceImpl();
    }

    private synchronized void runOnceImpl() {
        for (final String taskName : dest.getTasks()) {
            Future<Integer> resultFuture = runResultMap.get(taskName);
            if (resultFuture == null || resultFuture.isDone()) {
                log.warn("submit task:" + taskName);
                resultFuture = executorService.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        try {
                            int result = dest.exec(taskName, processListener);
                            log.warn("task:" + taskName + " has finished, sync count:" + result);
                            return result;
                        } catch (Exception e) {
                            log.error("error", e);
                            return null;
                        }
                    }
                });
                runResultMap.put(taskName, resultFuture);
            } else {
                log.error("task: " + taskName + " has running...");
            }
        }
    }

    public void setProcessListener(ProcessListener processListener) {
        this.processListener = processListener;
    }
}
