package com.github.pister.dbsync.scan;

import com.github.pister.dbsync.row.RowProcessor;
import com.github.pister.dbsync.rt.Row;
import com.github.pister.dbsync.util.CollectionUtil;
import com.github.pister.dbsync.util.StringUtil;
import com.github.pister.dbsync.config.TableConfig;
import com.github.pister.dbsync.rt.FieldValue;
import com.github.pister.dbsync.rt.Pagination;
import com.github.pister.dbsync.rt.SqlContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class LongPkSqlGenerator extends SqlGenerator {

    public LongPkSqlGenerator(MagicDb magicDb) {
        super(magicDb);
    }

    protected SqlContext getCondition(Pagination pagination, TableConfig tableConfig, String pkName, String extSqlCondition) throws SQLException {
        List<Long> ids = getNextIdsValue(pagination, tableConfig, pkName, extSqlCondition);
        if (CollectionUtil.isEmpty(ids)) {
            return null;
        }
        List<String> questions = CollectionUtil.dup("?", ids.size());
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        sb.append(pkName);
        sb.append(" in (");
        sb.append(CollectionUtil.join(questions, ","));
        sb.append(" )");
        SqlContext sqlContext = new SqlContext();
        sqlContext.setSql(sb.toString());
        sqlContext.setParams(ids.toArray(new Long[0]));
        return sqlContext;
    }

    @Override
    public Pagination getNextPagination(Pagination pagination, List<Row> prevRows) {
        if (pagination == null) {
            return pagination;
        }
        Number prevMaxId = getMaxId(prevRows);
        if (prevMaxId == null) {
            return null;
        }
        pagination.setValue(prevMaxId.longValue());
        return pagination;
    }

    private Number getMaxId(List<Row> prevRows) {
        if (CollectionUtil.isEmpty(prevRows)) {
            return null;
        }

        Number maxId = 0L;
        for (Row row : prevRows) {
            FieldValue fieldValue = (FieldValue) row.getPk();
            Number id = (Number) fieldValue.getValue();
            maxId = getMaxValue(maxId, id, fieldValue.getColumn().getSqlType());
        }
        return maxId;
    }

    private static Number getMaxValue(Number prevMaxId, Number newId, int sqlType) {
        switch (sqlType) {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.BIT:
                return Math.max(prevMaxId.intValue(), newId.intValue());
            case Types.BIGINT:
                return Math.max(prevMaxId.longValue(), newId.longValue());
            default:
                throw new RuntimeException("not support type:" + sqlType);
        }
    }

    private List<Long> getNextIdsValue(Pagination pagination, TableConfig tableConfig, String pkName, String extSqlCondition) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select ");
        stringBuilder.append(pkName);
        stringBuilder.append(" from ");
        stringBuilder.append(tableConfig.getTableName());
        stringBuilder.append(" where ");
        stringBuilder.append(pkName);
        stringBuilder.append(" > ? ");
        if (!StringUtil.isEmpty(tableConfig.getUpdatedField())) {
            stringBuilder.append(" and  ");
            stringBuilder.append(tableConfig.getUpdatedField());
            stringBuilder.append(" >= ? ");
        }
        if (StringUtil.isNotEmpty(extSqlCondition)) {
            stringBuilder.append(" AND (");
            stringBuilder.append(extSqlCondition);
            stringBuilder.append(" )");
        }
        stringBuilder.append(" order by ");
        stringBuilder.append(pkName);
        stringBuilder.append(" asc limit ?");
        final String sql = stringBuilder.toString();
        Object[] params = toParams(pagination, tableConfig);
        IdRowProcessor idRowProcessor = new IdRowProcessor(pkName);
        magicDb.getDbPool().executeQuery(tableConfig.getDbConfig(), idRowProcessor, sql, params);
        return idRowProcessor.getIds();
    }

    private static class IdRowProcessor implements RowProcessor<Object> {
        private List<Long> ids = CollectionUtil.newArrayList();
        private String pkName;

        public IdRowProcessor(String pkName) {
            this.pkName = pkName;
        }

        @Override
        public Object processRow(ResultSet rs) throws SQLException {
            long id = rs.getLong(pkName);
            ids.add(id);
            return null;
        }

        public List<Long> getIds() {
            return ids;
        }
    }

    private Object[] toParams(Pagination pagination, TableConfig tableConfig) {
        Object value = pagination.getValue();
        if (value == null) {
            value = 0L;
        }
        long id = ((Number) value).longValue();
        if (!StringUtil.isEmpty(tableConfig.getUpdatedField())) {
            return new Object[]{id, pagination.getLastModified(), pagination.getPageSize()};
        } else {
            return new Object[]{id, pagination.getPageSize()};

        }
    }

}
