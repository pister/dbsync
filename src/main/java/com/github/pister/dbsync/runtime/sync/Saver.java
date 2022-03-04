package com.github.pister.dbsync.runtime.sync;

import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.common.db.DbPool;
import com.github.pister.dbsync.runtime.exec.RichSql;
import com.github.pister.dbsync.runtime.exec.Row;
import com.github.pister.dbsync.common.tools.util.CollectionUtil;
import com.github.pister.dbsync.common.tools.util.StringUtil;
import com.github.pister.dbsync.common.tools.util.assist.Transformer;
import com.github.pister.dbsync.runtime.exec.FieldValue;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by songlihuang on 2017/7/12.
 */
public class Saver {

    private DbPool dbPool;

    public Saver(DbPool dbPool) {
        this.dbPool = dbPool;
    }

    public int insertOrUpdate(DbConfig dbConfig, String tableName, Row row) throws SQLException {
        RichSql richSql = makeSql(tableName, row);
        return dbPool.executeUpdate(dbConfig, richSql.getSql(), richSql.getParams());
    }

    private RichSql makeSql(String tableName, Row row) {
        Map<String, FieldValue> fieldValueMap = row.getFields();
        List<String> columnNames = CollectionUtil.newArrayList();
        List<String> columnNamesWithoutUnique = CollectionUtil.newArrayList();
        List<Object> values = CollectionUtil.newArrayList();
        List<Object> valuesWithoutUnique = CollectionUtil.newArrayList();
        final String pkName = row.getPkName();
        final Set<String> uniqueColumns = row.getUniqueColumns();

        for (Map.Entry<String, FieldValue> entry : fieldValueMap.entrySet()) {
            columnNames.add(entry.getKey());
            values.add(entry.getValue().getValue());
            if (StringUtil.equals(pkName, entry.getKey())) {
                continue;
            }
            if (!CollectionUtil.isEmpty(uniqueColumns) && uniqueColumns.contains(entry.getKey())) {
                continue;
            }
            columnNamesWithoutUnique.add(entry.getKey());
            valuesWithoutUnique.add(entry.getValue().getValue());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("insert into ");
        stringBuilder.append("`" + tableName + "`");
        stringBuilder.append(" ( ");
        String fieldsList = CollectionUtil.join(columnNames, ",", (Transformer<String, String>) object -> "`" + object + "`");
        stringBuilder.append(fieldsList);
        stringBuilder.append(" ) values (");
        String ques = CollectionUtil.join(CollectionUtil.dup("?", values.size()), ",");
        stringBuilder.append(ques);
        stringBuilder.append(" ) on duplicate key update ");

        String updateList = CollectionUtil.join(columnNamesWithoutUnique, ",", (Transformer<String, String>) object -> "`" + object + "` = ?");

        stringBuilder.append(updateList);

        String sql = stringBuilder.toString();

        RichSql richSql = new RichSql();
        richSql.setSql(sql);
        List<Object> paramsList = CollectionUtil.newArrayList(values);
        paramsList.addAll(valuesWithoutUnique);
        Object[] params = paramsList.toArray(new Object[0]);
        richSql.setParams(params);
        return richSql;
    }


}
