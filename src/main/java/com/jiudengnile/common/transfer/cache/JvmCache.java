package com.jiudengnile.common.transfer.cache;

import wint.lang.utils.DateUtil;
import wint.lang.utils.MapUtil;

import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/5
 * Time: 上午11:10
 */
public class JvmCache {

    private Map<String, SoftReference<ObjectWithExpire>> cacheHolder = MapUtil.newConcurrentHashMap();

    public Object get(String key) {
        SoftReference<ObjectWithExpire> softReference = cacheHolder.get(key);
        if (softReference == null) {
            return null;
        }
        ObjectWithExpire objectWithExpire = softReference.get();
        if (objectWithExpire == null) {
            return null;
        }

        if (objectWithExpire.isExpired()) {
            return null;
        }

        return objectWithExpire.getObject();
    }

    public void set(String key, Object object, int expireInSeconds) {
        Date expireDate = DateUtil.addSecond(new Date(), expireInSeconds);
        ObjectWithExpire owe = new ObjectWithExpire(object, expireDate.getTime());
        cacheHolder.put(key, new SoftReference<ObjectWithExpire>(owe));
    }

    public void delete(String key) {
        cacheHolder.remove(key);
    }

    public void clearAll() {
        cacheHolder.clear();
    }
}