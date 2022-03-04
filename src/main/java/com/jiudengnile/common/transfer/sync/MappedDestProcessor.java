package com.jiudengnile.common.transfer.sync;

import com.jiudengnile.common.transfer.aop.RowInterceptorResult;
import com.jiudengnile.common.transfer.aop.RowInterceptorWrapper;
import com.jiudengnile.common.transfer.client.Saver;
import com.jiudengnile.common.transfer.config.Column;
import com.jiudengnile.common.transfer.config.Columns;
import com.jiudengnile.common.transfer.config.DbConfig;
import com.jiudengnile.common.transfer.config.TableConfig;
import com.jiudengnile.common.transfer.config.mapping.table.MappedTable;
import com.jiudengnile.common.transfer.config.mapping.table.ShardMappedTable;
import com.jiudengnile.common.transfer.config.mapping.table.SingleMappedTable;
import com.jiudengnile.common.transfer.db.DbPool;
import com.jiudengnile.common.transfer.rt.FieldValue;
import com.jiudengnile.common.transfer.rt.ResultRow;
import com.jiudengnile.common.transfer.rt.Row;
import com.jiudengnile.common.transfer.scan.MagicDb;
import com.jiudengnile.common.transfer.shard.ShardInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wint.lang.utils.CollectionUtil;
import wint.lang.utils.MapUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2021/1/25.
 */
public class MappedDestProcessor extends DbPoolQueryProcessor implements DestProcessor {

    private static final Logger log = LoggerFactory.getLogger(MappedDestProcessor.class);

    private List<DbConfig> dbConfigList;

    private Saver saver;

    private MagicDb magicDb;

    public MappedDestProcessor(List<DbConfig> localDbConfigs, Saver saver, MagicDb magicDb, DbPool dbPool) {
        super(dbPool, localDbConfigs);
        this.dbConfigList = localDbConfigs;
        this.saver = saver;
        this.magicDb = magicDb;
    }

    @Override
    public List<ResultRow> saveRows(MappedTable mappedTable, List<Row> rows, RowInterceptorWrapper rowInterceptorWrapper) throws SQLException {
        if (CollectionUtil.isEmpty(rows)) {
            return CollectionUtil.newArrayList(0);
        }
        List<ResultRow> resultRows = CollectionUtil.newArrayList(rows.size());
        for (Row row : rows) {
            ResultRow resultRow = saveRow(mappedTable, row, rowInterceptorWrapper);
            if (resultRow == null) {
                continue;
            }
            resultRows.add(resultRow);
        }
        return resultRows;
    }

    @Override
    public List<ResultRow> saveRows(MappedTable mappedTable, List<Row> rows) throws SQLException {
        return saveRows(mappedTable, rows, null);
    }

    private ResultRow saveRow(MappedTable mappedTable, Row row, RowInterceptorWrapper rowInterceptorWrapper) throws SQLException {
        if (rowInterceptorWrapper != null) {
            RowInterceptorResult interceptorResult = rowInterceptorWrapper.getRowInterceptor().onBefore(row, rowInterceptorWrapper.getAopContext());
            if (interceptorResult.isIgnore()) {
                return null;
            }
            row = interceptorResult.getRow();
        }
        String tableName;
        DbConfig dbConfig;
        if (mappedTable.isShardSupport()) {
            ShardMappedTable shardMappedTable = (ShardMappedTable) mappedTable;
            ShardInfo shardInfo = shardMappedTable.route(row);
            dbConfig = dbConfigList.get(shardInfo.getDatabaseIndex());
            tableName = shardMappedTable.formatTableName(shardInfo.getDatabaseIndex(), shardInfo.getTableIndex());
        } else {
            SingleMappedTable singleMappedTable = (SingleMappedTable) mappedTable;
            tableName = mappedTable.getLocalTable();
            dbConfig = dbConfigList.get(singleMappedTable.getDbIndex());
        }
        Row localRow = translateRow(dbConfig, mappedTable, tableName, row);
        int returnValue = saver.insertOrUpdate(dbConfig, tableName, localRow);
        ResultRow resultRow = new ResultRow(localRow, returnValue);
        if (rowInterceptorWrapper != null) {
            rowInterceptorWrapper.getRowInterceptor().onAfter(resultRow, rowInterceptorWrapper.getAopContext());
        }
        return resultRow;
    }

    private Row translateRow(DbConfig dbConfig, MappedTable mappedTable, String localTable, Row remoteRow) throws SQLException {
        Map<String, String> columnMapping = mappedTable.getColumnNameMapping();
        TableConfig tableConfig = new TableConfig();
        tableConfig.setDbConfig(dbConfig);
        tableConfig.setTableName(localTable);
        Columns localColumns = magicDb.getColumns(tableConfig);

        Row ret = new Row();

        Map<String, FieldValue> remoteValues = remoteRow.getFields();
        for (Column column : localColumns.getColumns()) {
            String mappedColumn = mappedColumn(columnMapping, column.getName());
            FieldValue fieldValue = remoteValues.get(mappedColumn);
            if (fieldValue == null) {
                // throw new RuntimeException("can not found column from remote:" + mappedColumn);
                // the column will be ignored.
                continue;
            }
            FieldValue localFieldValue = new FieldValue();
            localFieldValue.setColumn(column);
            localFieldValue.setValue(fieldValue.getValue());
            ret.addField(column.getName(), localFieldValue);
        }
        ret.setAttachment(remoteRow.getAttachment());
        ret.setUniqueColumns(remoteRow.getUniqueColumns());
        ret.setPkName(localColumns.getPkName());
        return ret;
    }

    private static String mappedColumn(Map<String, String> columnMapping, String column) {
        if (MapUtil.isEmpty(columnMapping)) {
            return column;
        }
        String mappedName = columnMapping.get(column);
        if (mappedName != null) {
            return mappedName;
        }
        return column;
    }
}
