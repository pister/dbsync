package com.github.pister.dbsync.endpoint.auth;

import com.github.pister.dbsync.common.security.DigestUtil;
import com.github.pister.dbsync.common.tools.util.*;
import com.github.pister.dbsync.endpoint.remoting.Request;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by songlihuang on 2022/3/5.
 */
public class AuthUtil {

    public static String makeAuthToken(Request request, String appSecret) {
        String raw = appSecret + ";" + request.getServiceName() + ";" + request.getMethodName() + ";" + join(request.getArgs(), ",") + ";" + appSecret;
        return DigestUtil.sha256hex(raw.getBytes(Charset.defaultCharset()));
    }

    public static String join(Object[] values, String sep) {
        if (values == null || values.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(1024);
        boolean first = true;
        for (Object value : values) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(",");
            }
            stringBuilder.append(objectToValue(value));
        }
        return stringBuilder.toString();
    }

    private static String objectToValue(Object value) {
        if (value == null) {
            return "<null>";
        }
        if (value instanceof String) {
            return value.toString();
        }
        if (value instanceof Float || value instanceof Double) {
            long v = Double.doubleToLongBits(((Number) value).doubleValue());
            return String.valueOf(v);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Date) {
            return DateUtil.formatFullDate(value);
        }
        if (value instanceof Collection) {
            String values = CollectionUtil.join((Collection) value, ",");
            return "[" + values + "]";
        }
        if (value instanceof Map) {
            String values = MapUtil.join(new TreeMap<>((Map) value), "=", "&", AuthUtil::objectToValue, AuthUtil::objectToValue);
            return "{" + values + "}";
        }
        if (value.getClass().isArray()) {
            String values = ArrayUtil.join(value, ",", AuthUtil::objectToValue);
            return "[" + values + "]";
        }
        // other object
        Map<String, Object> objectMap = PropertyUtil.objectToMap(value);
        String values = MapUtil.join(new TreeMap<>(objectMap), "=", "&", AuthUtil::objectToValue, AuthUtil::objectToValue);
        return "{" + values + "}";
    }

}
