package com.jiudengnile.common.transfer;

import com.jiudengnile.common.transfer.base.AbstractPoint;
import com.jiudengnile.common.transfer.base.DbTable;
import com.jiudengnile.common.transfer.config.Columns;
import com.jiudengnile.common.transfer.config.DbConfig;
import com.jiudengnile.common.transfer.config.TableConfig;
import com.jiudengnile.common.transfer.db.DbPool;
import com.jiudengnile.common.transfer.rt.Pagination;
import com.jiudengnile.common.transfer.rt.RichSql;
import com.jiudengnile.common.transfer.rt.Row;
import com.jiudengnile.common.transfer.rt.ScanPageResult;
import com.jiudengnile.common.transfer.scan.MagicDb;
import com.jiudengnile.common.transfer.scan.Scanner;
import com.jiudengnile.common.transfer.sync.DbPoolQueryProcessor;
import com.jiudengnile.common.transfer.sync.DestProcessor;
import com.jiudengnile.common.transfer.sync.QueryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wint.help.sql.RowProcessor;
import wint.lang.magic.Transformer;
import wint.lang.utils.CollectionUtil;
import wint.lang.utils.MapUtil;
import wint.lang.utils.StringUtil;
import wint.lang.utils.filter.Filter;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class DefaultTransferServer extends AbstractPoint implements TransferServer {

    private static final Logger log = LoggerFactory.getLogger(DefaultTransferServer.class);

    private List<DbConfig> dbConfigList = CollectionUtil.newArrayList();

    private DbPool dbPool;

    private MagicDb magicDb;

    private Scanner scanner;

    private Map<Integer, List<TableConfig>> /*dbIndex ==>List<TableConfig> */ tableConfigListMap;

    private Map<DbTable, TableConfig> namedTableConfig;

    public void init() throws SQLException {
        dbPool = new DbPool();
        magicDb = new MagicDb(dbPool);
        scanner = new Scanner(magicDb);
        tableConfigListMap = MapUtil.newHashMap();
        namedTableConfig = MapUtil.newHashMap();


        for (int i = 0, len = dbConfigList.size(); i < len; i++) {
            DbConfig dbConfig = dbConfigList.get(i);
            List<String> tableNames = magicDb.listTables(dbConfig);
            List<TableConfig> tableConfigList = CollectionUtil.newArrayList(tableNames.size());
            for (String tableName : tableNames) {
                TableConfig tableConfig = createTableConfig(tableName, i, dbConfig);
                tableConfigList.add(tableConfig);
                namedTableConfig.put(new DbTable(i, tableName), tableConfig);
            }
            tableConfigListMap.put(i, tableConfigList);
        }
        log.warn("DefaultTransferServer init success.");
    }


    @Override
    public ScanPageResult fetchForPage(int dbIndex, String tableName, Pagination pagination, String extSqlCondition) {
        try {
            TableConfig tableConfig = namedTableConfig.get(new DbTable(dbIndex, tableName));
            if (tableConfig == null) {
                ScanPageResult scanPageResult = new ScanPageResult();
                scanPageResult.setSuccess(false);
                scanPageResult.setMessage("table not exist, db: " + dbIndex + ", table: " + tableName);
                return scanPageResult;
            }
            ScanPageResult scanPageResult = scanner.fetchForPage(pagination, tableConfig, extSqlCondition);
            scanPageResult.setSuccess(true);
            return scanPageResult;
        } catch (Exception e) {
            log.error("error", e);
            ScanPageResult scanPageResult = new ScanPageResult();
            scanPageResult.setSuccess(false);
            scanPageResult.setMessage("system error:" + e.getMessage());
            return scanPageResult;
        }
    }

    public List<String> dbNameList() {
        return CollectionUtil.transformList(dbConfigList, new Transformer<DbConfig, String>() {
            @Override
            public String transform(DbConfig object) {
                return object.getDbName();
            }
        });
    }

    public List<String> tableNameList(final int dbIndex) {
        List<TableConfig> tableConfigList = tableConfigListMap.get(dbIndex);
        if (CollectionUtil.isEmpty(tableConfigList)) {
            return CollectionUtil.newArrayList();
        }
        return CollectionUtil.transformList(tableConfigList, new Transformer<TableConfig, String>() {
            @Override
            public String transform(TableConfig object) {
                return object.getTableName();
            }
        });
    }

    @Override
    public <T> List<T> executeForList(int dbIndex, RowProcessor<T> rowProcessor, String sql, Object[] params) throws SQLException {
        DbConfig dbConfig = dbConfigList.get(dbIndex);
        return magicDb.getDbPool().executeForList(dbConfig, rowProcessor, sql, params);
    }

    @Override
    public <T> T executeForObject(int dbIndex, RowProcessor<T> rowProcessor, String sql, Object[] params) throws SQLException {
        DbConfig dbConfig = dbConfigList.get(dbIndex);
        return magicDb.getDbPool().executeForObject(dbConfig, rowProcessor, sql, params);
    }

    @Override
    public Columns getColumns(int dbIndex, String tableName) {
        TableConfig tableConfig = namedTableConfig.get(new DbTable(dbIndex, tableName));
        if (tableConfig == null) {
            return null;
        }
        try {
            return magicDb.getColumns(tableConfig);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public QueryProcessor createQueryProcessor() {
        return new DbPoolQueryProcessor(dbPool, dbConfigList);
    }

    /**
     * 请注意顺序，使用的过程要按0,1,2,3...序列获取
     *
     * @param dbConfig
     */
    public void addDbConfig(DbConfig dbConfig) {
        dbConfigList.add(dbConfig);
    }


}
