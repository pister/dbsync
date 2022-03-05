package com.github.pister.dbsync.common.tools.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by songlihuang on 2022/3/5.
 */
public class PropertyUtil {

    private static final int GET_LENGTH = 3;

    public static Map<String, Object> objectToMap(Object object) {
        if (object == null) {
            return null;
        }
        Map<String, Object> ret = MapUtil.newHashMap();
        Class clazz = object.getClass();
        for (Method method : clazz.getMethods()) {
            String methodName = method.getName();
            if (!methodName.startsWith("get")) {
                continue;
            }
            if (methodName.length() <= GET_LENGTH) {
                continue;
            }
            if (method.getParameters().length > 0) {
                continue;
            }
            if (method.getReturnType() == null || method.getReturnType().equals(Void.TYPE)) {
                continue;
            }
            String propertyName = StringUtil.lowercaseFirstLetter(methodName.substring(GET_LENGTH));
            if ("class".equals(propertyName)) {
                ret.put(propertyName, clazz.getName());
                continue;
            }
            try {
                Object value = method.invoke(object);
                ret.put(propertyName, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getTargetException());
            }
        }
        return ret;
    }


}
