package com.github.pister.dbsync.endpoint.auth;

/**
 * Created by songlihuang on 2022/3/5.
 */
public interface AppSecretProvider {

    String getAppSecret(String appId);

}
