package com.github.pister.dbsync;

import com.github.pister.dbsync.aop.AopContext;
import com.github.pister.dbsync.aop.BatchInterceptor;
import com.github.pister.dbsync.aop.InterceptorResult;
import com.github.pister.dbsync.config.mapping.RichTableConfig;
import com.github.pister.dbsync.config.mapping.TableTaskConfig;
import com.github.pister.dbsync.config.mapping.table.MappedTable;
import com.github.pister.dbsync.id.Sequence;
import com.github.pister.dbsync.rt.ResultRow;
import com.github.pister.dbsync.rt.Row;
import com.github.pister.dbsync.sync.DestProcessor;
import com.github.pister.dbsync.util.CollectionUtil;
import com.github.pister.dbsync.util.MySqlUtil;
import com.github.pister.dbsync.util.assist.Transformer;
import com.github.pister.dbsync.config.mapping.MappedTableUtils;
import com.github.pister.dbsync.rt.FieldValue;
import junit.framework.TestCase;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 * Created by songlihuang on 2021/2/18.
 */
public class TransferTest extends TestCase {


    private TransferServer initTransferServer() throws SQLException {
        DefaultTransferServer defaultTransferServer = new DefaultTransferServer();
        defaultTransferServer.addDbConfig(MySqlUtil.makeDbConfig("127.0.0.1:3306/sample", "root", "123456"));
        // defaultTransferServer.addDbConfig(MySqlUtil.makeDbConfig("127.0.0.1:3306/sample_other_source", "root", "123456"));
        defaultTransferServer.init();
        return defaultTransferServer;

    }

    public void testInitTransferServer() throws SQLException {
        TransferServer transferServer = initTransferServer();
        System.out.println(transferServer.dbNameList());
        System.out.println(transferServer.tableNameList(1));
    }

    public void testTransferSingleTable() throws Exception {
        TransferServer transferServer = initTransferServer();
        TableTransferClient tableTransferClient = new TableTransferClient();
        tableTransferClient.setTransferServer(transferServer);
        tableTransferClient.addLocalDb(MySqlUtil.makeDbConfig("127.0.0.1:3306/sample2", "root", "123456"));
        //  tableTransferClient.addLocalDb(MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_01", "trans_test_user", "trans_test_pwd"));
        tableTransferClient.addTableTaskConfig(TableTaskConfig.makeSingle("my_sample_task", 0, "sample_pen", 0, "sample_pen")
                );
        tableTransferClient.init();
        tableTransferClient.syncTable("my_sample_task");
    }
    /*


    public void testTransferShardsTable() throws Exception {
        TransferServer transferServer = initTransferServer();
        TableTransferClient tableTransferClient = new TableTransferClient();
        tableTransferClient.setTransferServer(transferServer);
        tableTransferClient.addLocalDb(MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_00", "trans_test_user", "trans_test_pwd"));
        tableTransferClient.addLocalDb(MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_01", "trans_test_user", "trans_test_pwd"));
        tableTransferClient.addTableTaskConfig(TableTaskConfig.makeOneTooManyShard("sports", 0, "qserver_sport", "trans_test_sport_%04d", "user_id", 2, 4));
        tableTransferClient.init();
        tableTransferClient.syncTable("sports");
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
                    System.out.println("after get:"  + input.getAttachment());

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
        SimpleMysqlTransfer mysqlTransfer = new SimpleMysqlTransfer();
        mysqlTransfer.addSourceDbConfig(MySqlUtil.makeDbConfig("test112.benshouyin.net/qserver", "qserver_user", "qserver_pwd"));
        mysqlTransfer.addSourceDbConfig(MySqlUtil.makeDbConfig("test112.benshouyin.net/qserver", "qserver_user", "qserver_pwd"));

        mysqlTransfer.addDestDbConfig(MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_00", "trans_test_user", "trans_test_pwd"));
        mysqlTransfer.addDestDbConfig(MySqlUtil.makeDbConfig("test112.benshouyin.net/trans_test_01", "trans_test_user", "trans_test_pwd"));

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