package com.github.pister.dbsync.endpoint.auth;

import com.github.pister.dbsync.endpoint.remoting.Request;
import junit.framework.TestCase;

import java.util.Date;

/**
 * Created by songlihuang on 2022/3/5.
 */
public class AuthUtilTest extends TestCase {
    public void testJoin() throws Exception {
        String s = AuthUtil.join(new Object[]{123, "xxx", new Request(), new Date(), new int[]{22,44}}, ",");
        System.out.println(s);
    }

}