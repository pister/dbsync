package com.github.pister.dbsync.cache;

import java.util.Date;

/**
 * User: huangsongli
 * Date: 16/10/28
 * Time: 下午12:58
 */
public class ObjectWithExpire {
    private long expireTime;
    private Object object;

    public ObjectWithExpire(Object object, long expireTime) {
        this.object = object;
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        return new Date().getTime() > expireTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
