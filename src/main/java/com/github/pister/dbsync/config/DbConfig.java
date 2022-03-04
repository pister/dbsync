package com.github.pister.dbsync.config;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class DbConfig {

    private String dbName;

    private String url;

    private String username;

    private String password;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DbConfig dbConfig = (DbConfig) o;

        if (dbName != null ? !dbName.equals(dbConfig.dbName) : dbConfig.dbName != null) return false;
        if (url != null ? !url.equals(dbConfig.url) : dbConfig.url != null) return false;
        if (username != null ? !username.equals(dbConfig.username) : dbConfig.username != null) return false;
        return password != null ? password.equals(dbConfig.password) : dbConfig.password == null;
    }

    @Override
    public int hashCode() {
        int result = dbName != null ? dbName.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
