package com.jiudengnile.common.transfer.scan;

import com.jiudengnile.common.transfer.config.TableConfig;
import com.jiudengnile.common.transfer.rt.Pagination;
import com.jiudengnile.common.transfer.rt.Row;
import com.jiudengnile.common.transfer.rt.SqlContext;
import wint.lang.utils.CollectionUtil;
import wint.lang.utils.StringUtil;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class StringPkSqlGenerator extends SqlGenerator {

    public static final String PREFIX_HUMAN_ASCII_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_=+~`!@#$%^&*()[]{};:'\",<.>/?\\|";

    public static final String PREFIX_ALPHA_NUM_ASCII_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static final String PREFIX_BASE64_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+/-=";

    public static final String PREFIX_HEX_SET = "abcdefABCDEFZ0123456789";

    private String prefixChars = PREFIX_BASE64_SET;

    private int prefixSize = 2;

    public StringPkSqlGenerator(MagicDb magicDb) {
        super(magicDb);
    }

    @Override
    protected SqlContext getCondition(Pagination pagination, TableConfig tableConfig, String pkName, String extSqlCondition) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ");
        stringBuilder.append(pkName);
        // 加binary的目的是mysql中Column Collate *_ci建表类型是不区分大小写的
        // 但是这里需要区分大小写
        stringBuilder.append(" like binary ? ");

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
        stringBuilder.append(" asc limit ?, ?");

        Object[] params = toParams(pagination, tableConfig);
        SqlContext sqlContext = new SqlContext();
        sqlContext.setSql(stringBuilder.toString());
        sqlContext.setParams(params);
        return sqlContext;
    }

    private Object[] toParams(Pagination pagination, TableConfig tableConfig) {
        StringPagination stringPagination = (StringPagination)pagination.getValue();
        if (stringPagination == null) {
            stringPagination = createDefaultPagination();
            pagination.setValue(stringPagination);
        }
        String prefix = stringPagination.getCurrentPrefix(prefixChars);
        String prefixPattern = prefix + "%";
        if (!StringUtil.isEmpty(tableConfig.getUpdatedField())) {
            return new Object[]{prefixPattern, pagination.getLastModified(), stringPagination.getStartRow(), pagination.getPageSize()};
        } else {
            return new Object[]{prefixPattern, stringPagination.getStartRow(), pagination.getPageSize()};
        }
    }

    @Override
    public Pagination getNextPagination(Pagination pagination, List<Row> prevRows) {
        if (pagination == null) {
            return null;
        }
        StringPagination stringPagination = (StringPagination)pagination.getValue();
        if (stringPagination == null) {
            return null;
        }
        if (CollectionUtil.isEmpty(prevRows)) {
            // 查询到没数据的时候切换下一前缀
            if (!stringPagination.tryIncrPrefixIndex(prefixChars)) {
                // 已经用完了
                return null;
            }
        } else {
            // 有数据的时候增加翻页
            stringPagination.addStartRow(pagination.getPageSize());
        }
        pagination.setValue(stringPagination);
        return pagination;
    }


    private StringPagination createDefaultPagination() {
        StringPagination stringPagination = new StringPagination();
        stringPagination.setStartRow(0);
        stringPagination.setPrefixIndexes(new int[prefixSize]);
        return stringPagination;
    }


    public static class StringPagination implements Serializable {
        private static final long serialVersionUID = 1756320827853664628L;
        private int[] prefixIndexes;
        private int startRow;

        public String getCurrentPrefix(String prefixChars) {
            StringBuilder stringBuilder = new StringBuilder(prefixIndexes.length);
            for (int index : prefixIndexes) {
                char c = prefixChars.charAt(index);
                stringBuilder.append(c);
            }
            return stringBuilder.toString();
        }

        public boolean tryIncrPrefixIndex(String prefixChars) {
            int len = prefixChars.length() - 1;
            int pos = prefixIndexes.length - 1;
            while (pos >= 0) {
                int index = prefixIndexes[pos];
                if (index < len) {
                    prefixIndexes[pos]++;
                    startRow = 0;
                    return true;
                }
                prefixIndexes[pos] = 0;
                pos--;
            }
            return false;
        }

        public int[] getPrefixIndexes() {
            return prefixIndexes;
        }

        public void setPrefixIndexes(int[] prefixIndexes) {
            this.prefixIndexes = prefixIndexes;
        }

        public int getStartRow() {
            return startRow;
        }

        public void addStartRow(int pageSize) {
            this.startRow += pageSize;
        }

        public void setStartRow(int startRow) {
            this.startRow = startRow;
        }

        @Override
        public String toString() {
            return "StringPagination{" +
                    "prefixIndexes=" + Arrays.toString(prefixIndexes) +
                    ", startRow=" + startRow +
                    '}';
        }
    }


    public void setPrefixChars(String prefixChars) {
        this.prefixChars = prefixChars;
    }

    public void setPrefixSize(int prefixSize) {
        this.prefixSize = prefixSize;
    }
}
