package com.github.pister.dbsync.rt;

import com.github.pister.dbsync.config.Column;
import com.github.pister.dbsync.util.CollectionUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class Row implements Serializable {

    private static final long serialVersionUID = 2042431927331690683L;
    private String pkName;

    private Set<String> uniqueColumns;

    private LinkedHashMap<String, FieldValue> fields = new LinkedHashMap<String, FieldValue>();

    public void addField(String fieldName, FieldValue fieldValue) {
        fields.put(fieldName, fieldValue);
    }

    public Map<String, FieldValue> getFields() {
        return fields;
    }

    private Object attachment;

    public void addField(String fieldName, Object value, int type) {
        addField(fieldName, new FieldValue(new Column(fieldName, type), value));
    }

    public Object getFieldValue(String fieldName) {
        FieldValue field = fields.get(fieldName);
        if (field == null) {
            return null;
        }
        return field.getValue();
    }

    public boolean copyColumnFrom(Row src, String fieldName) {
        return copyColumnFrom(src, fieldName, fieldName);
    }

    public boolean copyColumnFrom(Row src, String srcFieldName, String fieldName) {
        FieldValue fieldValue = src.getFields().get(srcFieldName);
        if (fieldValue == null) {
            return false;
        }
        addField(fieldName, fieldValue);
        return true;
    }


    public Row deepCopy() {
        Row row = new Row();
        row.pkName = pkName;
        for (Map.Entry<String, FieldValue> entry : fields.entrySet()) {
            FieldValue fieldValue = entry.getValue();
            int sqlType = fieldValue.getColumn().getSqlType();
            row.addField(entry.getKey(), fieldValue.getValue(), sqlType);
        }
        return row;
    }

    public List<String> getColumns() {
        return null;
    }

    public FieldValue getPk() {
        return fields.get(pkName);
    }

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    public void addUniqueColumn(String uniqueColumn) {
        if (uniqueColumns == null) {
            uniqueColumns = CollectionUtil.newHashSet();
        }
        uniqueColumns.add(uniqueColumn);
    }

    public Set<String> getUniqueColumns() {
        return uniqueColumns;
    }

    public void setUniqueColumns(Set<String> uniqueColumns) {
        this.uniqueColumns = uniqueColumns;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "Row{" +
                "pkName='" + pkName + '\'' +
                ", fields=" + fields +
                '}';
    }
}
