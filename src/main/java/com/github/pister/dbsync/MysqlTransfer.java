package com.github.pister.dbsync;

import com.github.pister.dbsync.sync.ProcessListener;
import com.github.pister.dbsync.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by songlihuang on 2021/2/18.
 */
public abstract class MysqlTransfer {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private ExecutorService executorService;

    private AtomicBoolean inited = new AtomicBoolean(false);

    private AtomicBoolean runningInterval = new AtomicBoolean(false);

    private ConcurrentMap<String, Future<Integer>> runResultMap = MapUtil.newConcurrentHashMap();

    private TableTransferClient dest;

    protected abstract TransferServer initTransferServer() throws SQLException;

    protected abstract TableTransferClient initTableTransferClient(TransferServer source) throws Exception;

    private ProcessListener processListener = new ProcessListener() {
        @Override
        public void onProcess(String taskName, int rows) {
            // nop
        }

        @Override
        public void onFinish(String taskName) {
            // nop
        }
    };

    public final void init() throws Exception {
        if (!inited.compareAndSet(false, true)) {
            throw new RuntimeException("can not call init more than once!");
        }
        TransferServer source = initTransferServer();
        TableTransferClient dest = initTableTransferClient(source);
        this.dest = dest;

        this.executorService = Executors.newFixedThreadPool(Math.min(dest.getTasks().size(), 5));

    }

    /**
     * 执行一次同步，可以通过手动调用多次
     */
    public boolean runOnce() {
        if (runningInterval.get()) {
           return false;
        }
        runOnceImpl();
        return true;
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
                            int result = dest.syncTable(taskName, processListener);
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

    /**
     * 自动定时同步
     *
     * @param seconds 每隔多少秒同步一次
     */
    public void runInterval(final int seconds) {
        if (seconds < 10) {
            throw new IllegalArgumentException("interval value must greater than " + seconds);
        }
        if (!runningInterval.compareAndSet(false, true)) {
            log.warn("runInterval has been started.");
            return;
        }
        new Thread() {
            public void run() {
                while (runningInterval.get()) {
                    runOnceImpl();
                    try {
                        Thread.sleep(seconds * 1000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                log.warn("run interval has been stopped.");
            }
        }.start();
    }

    public void stopInterval() {
        runningInterval.set(false);
    }

    public void setProcessListener(ProcessListener processListener) {
        this.processListener = processListener;
    }
}
