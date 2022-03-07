package com.github.pister.dbsync.endpoint.auth;

import com.github.pister.dbsync.common.tools.util.MapUtil;

import java.util.Map;

/**
 * Created by songlihuang on 2022/3/7.
 */
public class MapAppSecretProvider implements AppSecretProvider {

    private Map<String, String> appSecretMap;

    public MapAppSecretProvider(Map<String, String> appSecretMap) {
        this.appSecretMap = MapUtil.newHashMap(appSecretMap);
    }

    @Override
    public String getAppSecret(String appId) {
        return appSecretMap.get(appId);
    }
}
