package com.github.pister.dbsync.runtime.progress;

import com.github.pister.dbsync.common.io.FastByteArrayInputStream;
import com.github.pister.dbsync.common.io.FastByteArrayOutputStream;
import com.github.pister.dbsync.runtime.exec.Pagination;
import com.github.pister.dbsync.common.tools.util.FileUtil;
import com.github.pister.dbsync.common.tools.util.StringUtil;
import com.github.pister.dbsync.common.tools.util.DateUtil;
import com.github.pister.dbsync.common.tools.util.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class FileProgressManager implements ProgressManager {

    private static final String DATE_FMT = "yyyy-MM-dd HH:mm:ss";

    private File baseFile;

    public void init() {
        if (baseFile == null) {
            baseFile = new File(SystemUtil.USER_HOME + "/transfer/client/progress");
        }
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
    }

    @Override
    public void save(String taskName, TableProgress tableProgress) throws IOException {
        File file = new File(baseFile, taskName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        byte[] data = encode(tableProgress);
        FileUtil.writeContent(file, data);
    }

    @Override
    public TableProgress load(String taskName) throws IOException {
        File file = new File(baseFile, taskName);
        if (!file.exists()) {
            return null;
        }
        byte[] data = FileUtil.readContent(file);
        if (data == null) {
            return null;
        }
        return decode(data);
    }

    protected byte[] encode(TableProgress tableProgress) {

        Properties properties = new Properties();
        properties.put("taskName", tableProgress.getTaskName());
        properties.put("remoteDbIndex", String.valueOf(tableProgress.getRemoteDbIndex()));
        properties.put("remoteTable", String.valueOf(tableProgress.getRemoteTable()));
        properties.put("nextLastModifiedValue", DateUtil.formatDate(tableProgress.getNextLastModifiedValue(), DATE_FMT));
        Pagination pagination = tableProgress.getPagination();

        properties.put("pagination-pageSize", String.valueOf(pagination.getPageSize()));
        properties.put("pagination-value", String.valueOf(pagination.getValue()));

        if (pagination.getValue() instanceof String) {
            properties.put("pagination-valueType", "string");
        } else if (pagination.getValue() instanceof Long) {
            properties.put("pagination-valueType", "long");
        } else if (pagination.getValue() instanceof Integer) {
            properties.put("pagination-valueType", "int");
        } else if (pagination.getValue() == null) {
            properties.put("pagination-valueType", "finish");
        } else {
            throw new RuntimeException("not support value type, value:" + pagination.getValue());
        }
        properties.put("pagination-lastModified", DateUtil.formatDate(pagination.getLastModified(), DATE_FMT));
        FastByteArrayOutputStream bos = new FastByteArrayOutputStream(1024);
        try {
            properties.store(bos, "progress");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    protected TableProgress decode(byte[] b) {
        Properties properties = new Properties();
        try {
            properties.load(new FastByteArrayInputStream(b));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TableProgress tableProgress = new TableProgress();
        tableProgress.setTaskName(properties.getProperty("taskName"));
        tableProgress.setRemoteDbIndex(getPropertyInt(properties, "remoteDbIndex"));
        tableProgress.setRemoteTable(properties.getProperty("remoteTable"));
        tableProgress.setNextLastModifiedValue(getPropertyDate(properties, "nextLastModifiedValue"));
        Pagination pagination = new Pagination();
        pagination.setPageSize(getPropertyInt(properties, "pagination-pageSize"));
        String type = properties.getProperty("pagination-valueType");
        if (StringUtil.equals("string", type)) {
            pagination.setValue(properties.getProperty("pagination-value"));
        } else if (StringUtil.equals("long", type)) {
            pagination.setValue(getPropertyLong(properties, "pagination-value"));
        } else if (StringUtil.equals("int", type)) {
            pagination.setValue(getPropertyInt(properties, "pagination-value"));
        } else if (StringUtil.equals("finish", type)) {
            pagination.setValue(null);
        } else {
            throw new RuntimeException("not support pagination-valueType:" + type);
        }
        pagination.setLastModified(getPropertyDate(properties, "pagination-lastModified"));
        tableProgress.setPagination(pagination);
        return tableProgress;

    }

    private static int getPropertyInt(Properties properties, String name) {
        String value = properties.getProperty(name);
        if (value == null) {
            throw new RuntimeException("not value for properties:" + name);
        }
        return Integer.parseInt(value);
    }

    private static long getPropertyLong(Properties properties, String name) {
        String value = properties.getProperty(name);
        if (value == null) {
            throw new RuntimeException("not value for properties:" + name);
        }
        return Long.parseLong(value);
    }

    private static Date getPropertyDate(Properties properties, String name) {
        String value = properties.getProperty(name);
        if (value == null) {
            throw new RuntimeException("not value for properties:" + name);
        }
        return DateUtil.parseDate(value, DATE_FMT);
    }

    public void setBaseFile(String fileName) {
        baseFile = new File(fileName);
    }

}
