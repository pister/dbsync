package com.github.pister.dbsync.common.db.shard;

/**
 * User: huangsongli
 * Date: 16/4/29
 * Time: 上午9:54
 */
public final class RouteUtil {

    private RouteUtil() {
    }

    public static long getLongValue(Object value) {
        return Math.abs(getLongValueImpl(value));
    }

    public static long getLongValueImpl(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            return stringHashCode((String) value);
        }
        return value.hashCode();
    }


    /**
     * 不直接使用String.hashCode()的原因是，
     * 万一jdk的String.hashCode算法或是参数变化，导致hash值变化
     *
     * @param s
     * @return
     */
    private static int stringHashCode(String s) {
        int h = 0;
        int len = s.length();
        if (len > 0) {
            int off = 0;
            char val[] = s.toCharArray();
            for (int i = 0; i < len; i++) {
                h = 31 * h + val[off++];
            }
        }
        return h;
    }

}
