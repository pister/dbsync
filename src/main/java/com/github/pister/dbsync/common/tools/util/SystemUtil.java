package com.github.pister.dbsync.common.tools.util;


import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class SystemUtil {

    public static final String LINE_SEPARATOR = System.getProperty("line.separater", "\n");

    public static final String SYSTEM_INFO = getSystemInfo();

    public static final int PID = getPid();

    public static final String USER_HOME = getUserHome();

    public static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getSystemInfo() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        return runtime.getName();
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }

}