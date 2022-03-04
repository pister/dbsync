package com.jiudengnile.common.transfer.util;

import com.jiudengnile.common.transfer.config.DbConfig;

/**
 * Created by songlihuang on 2021/1/25.
 */
public class MySqlUtil {

    private static final String urlPattern = "jdbc:mysql://%s?useUnicode=true&amp;characterEncoding=utf8&amp;zeroDateTimeBehavior=convertToNull&amp;transformedBitIsBoolean=true";

    private static String makeUrl(String url) {
        return String.format(urlPattern, url);
    }

    public static DbConfig makeDbConfig(String shortUrl, String username, String password) {
        // 127.0.0.1/dbname
        String[] parts = shortUrl.split("/");
        String dbName = parts[1];
        DbConfig dbConfig = new DbConfig();
        dbConfig.setUsername(username);
        dbConfig.setPassword(password);
        dbConfig.setUrl(makeUrl(shortUrl));
        dbConfig.setDbName(dbName);
        return dbConfig;
    }

}
