package com.github.pister.dbsync;

import com.github.pister.dbsync.endpoint.client.DbSyncClient;
import com.github.pister.dbsync.endpoint.server.DefaultSyncServer;
import com.github.pister.dbsync.endpoint.server.SyncServer;
import com.github.pister.dbsync.runtime.aop.AopContext;
import com.github.pister.dbsync.runtime.aop.BatchInterceptor;
import com.github.pister.dbsync.runtime.aop.InterceptorResult;
import com.github.pister.dbsync.config.mapping.RichTableConfig;
import com.github.pister.dbsync.config.mapping.TableTaskConfig;
import com.github.pister.dbsync.config.mapping.table.MappedTable;
import com.github.pister.dbsync.common.db.seq.Sequence;
import com.github.pister.dbsync.runtime.exec.ResultRow;
import com.github.pister.dbsync.runtime.exec.Row;
import com.github.pister.dbsync.runtime.sync.DestProcessor;
import com.github.pister.dbsync.common.tools.util.CollectionUtil;
import com.github.pister.dbsync.common.tools.util.MySqlUtil;
import com.github.pister.dbsync.common.tools.util.assist.Transformer;
import com.github.pister.dbsync.config.mapping.MappedTableUtils;
import com.github.pister.dbsync.runtime.exec.FieldValue;
import junit.framework.TestCase;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 * Created by songlihuang on 2021/2/18.
 */
public class DbSyncTest extends TestCase {

    private SyncServer initTransferServer() throws SQLException {
        DefaultSyncServer defaultTransferServer = new DefaultSyncServer();
        defaultTransferServer.registerDbConfig(0, "127.0.0.1:3306/sample", "root", "123456");
        // defaultTransferServer.registerDbConfig(1, MySqlUtil.makeDbConfig("127.0.0.1:3306/sample_other_source", "root", "123456"));
        defaultTransferServer.init();
        return defaultTransferServer;

    }

    public void testInitTransferServer() throws SQLException {
        SyncServer syncServer = initTransferServer();
        System.out.println(syncServer.dbNameList());
        System.out.println(syncServer.tableNameList(1));
    }

    public void testTransferSingleTable() throws Exception {
        SyncServer syncServer = initTransferServer();
        DbSyncClient dbSyncClient = new DbSyncClient();
        dbSyncClient.setSyncServer(syncServer);
        dbSyncClient.registerLocalDb(0, "127.0.0.1:3306/sample2", "root", "123456");
        //  dbSyncClient.addLocalDb(MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_01", "trans_test_user", "trans_test_pwd"));
        dbSyncClient.addTableTaskConfig(TableTaskConfig.makeSingle("my_sample_task", 0, "sample_pen", 0, "sample_pen"));

        dbSyncClient.init();

        dbSyncClient.exec("my_sample_task");
    }
    /*


    public void testTransferShardsTable() throws Exception {
        SyncServer transferServer = initTransferServer();
        DbSyncClient tableTransferClient = new DbSyncClient();
        tableTransferClient.setSyncServer(transferServer);
        tableTransferClient.addLocalDb(MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_00", "trans_test_user", "trans_test_pwd"));
        tableTransferClient.addLocalDb(MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_01", "trans_test_user", "trans_test_pwd"));
        tableTransferClient.addTableTaskConfig(TableTaskConfig.makeOneTooManyShard("sports", 0, "qserver_sport", "trans_test_sport_%04d", "user_id", 2, 4));
        tableTransferClient.init();
        tableTransferClient.exec("sports");
    }
*/

    private BatchInterceptor myBatchInterceptor = new BatchInterceptor() {

        MappedTable abcMappedTable = MappedTableUtils.makeMappedTable(RichTableConfig.makeOneTooManyShard("trans_test_abc_%04d", "id", 2, 4, 0));

        @Override
        public InterceptorResult onBefore(Row input, AopContext aopContext) throws SQLException {
            FieldValue mobileValue = input.getFields().get("mobile");
            FieldValue nickname = input.getFields().get("nickname");
            if (nickname != null && nickname.getValue() != null) {
                String nick = nickname.getValue().toString();
                mobileValue.setValue(nick.toUpperCase());
            }
            FieldValue idValue = input.getFields().get("id");

            input.setAttachment("aaa:" + idValue.getValue());
            /*
            String openId = aopContext.getSourceQueryProcessor().queryForObject(0, new RowProcessor<String>() {
                @Override
                public String processRow(ResultSet rs) throws SQLException {
                    return rs.getString("open_id");
                }
            }, "select user_id, open_id from qserver_wx_user where user_id = ?", new Object[]{idValue.getValue()});
            input.addField("open_id", openId, Types.VARCHAR);
            */
            return new InterceptorResult(false, input);
        }

        @Override
        public void onAfter(List<ResultRow> rows, AopContext aopContext) throws SQLException {
            System.out.println("enter onAfter.......");

            final DestProcessor destProcessor = aopContext.getLocalRowsProcessor();
            final Sequence sequence = aopContext.getSequence("trans_test_sport");

            // 这里完成新的数据写入
            List<Row> myRows = CollectionUtil.transformList(rows, new Transformer<ResultRow, Row>() {
                @Override
                public Row transform(ResultRow inputResult) {
                    Row input = inputResult.getRow();
                    System.out.println("after get:" + input.getAttachment());

                    long id = sequence.nextValue();
                    Row row = new Row();
                    row.setPkName("id");
                    row.addUniqueColumn("uniq_key");
                    row.addField("id", id, Types.BIGINT);
                    row.addField("gmt_modified", new Timestamp(new Date().getTime()), Types.TIMESTAMP);
                    row.addField("gmt_create", new Timestamp(new Date().getTime()), Types.TIMESTAMP);
                    row.addField("length_in_time", 240, Types.INTEGER);
                    row.addField("name", String.format("sport-id: " + input.getFieldValue("id")), Types.VARCHAR);
                    row.addField("uniq_key", String.format("sport-key: " + input.getFieldValue("id")), Types.VARCHAR);

                    return row;
                }
            });


            /*
            String name = (String)destProcessor.queryForObject(abcMappedTable, new RowProcessor<Object>() {
                @Override
                public String processRow(ResultSet rs) throws SQLException {
                    return rs.getString("name");
                }
            }, "select name from $table$ where id = ?", 1L, 1L);

            System.out.println("exist name:" + name);
            */

            destProcessor.saveRows(abcMappedTable, myRows);
            System.out.println("finish onAfter!!!!");
        }
    };

    public void testMysqlTransfer() throws Exception {
        DbSyncManager mysqlTransfer = new DbSyncManager();
        mysqlTransfer.registerSourceDbConfig(0, MySqlUtil.makeDbConfig("test112.benshouyin.net/qserver", "qserver_user", "qserver_pwd"));
        mysqlTransfer.registerSourceDbConfig(1, MySqlUtil.makeDbConfig("test112.benshouyin.net/qserver", "qserver_user", "qserver_pwd"));

        mysqlTransfer.registerDestDbConfig(0, MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_00", "trans_test_user", "trans_test_pwd"));
        mysqlTransfer.registerDestDbConfig(1, MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_01", "trans_test_user", "trans_test_pwd"));

        // 1 => 1
        mysqlTransfer.addTask(TableTaskConfig.makeSingle("user-table3", 0, "qserver_user", 0, "trans_test_user")
                .mappingColumn("nickname2", "nickname")
                .onlyFullDump(true)
                .sourceExtCondition("deleted = 0"));

        // 1 => N
        //    mysqlTransfer.addTask(TableTaskConfig.makeOneTooManyShard("sportsc", 0, "qserver_sport", "trans_test_sport_%04d", "user_id", 2, 4).batchInterceptor(myBatchInterceptor));

        mysqlTransfer.setDestSeqDbIndex(0);

        mysqlTransfer.setIgnoreCheck(true);
        mysqlTransfer.init();

        mysqlTransfer.runOnce();
        //mysqlTransfer.runInterval(10);

        Thread.sleep(10 * 60 * 1000);
    }

}