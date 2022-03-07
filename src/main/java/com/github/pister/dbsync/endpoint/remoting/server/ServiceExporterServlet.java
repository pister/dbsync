package com.github.pister.dbsync.endpoint.remoting.server;

import com.github.pister.dbsync.common.tools.util.MapUtil;
import com.github.pister.dbsync.endpoint.remoting.Response;
import com.github.pister.dbsync.common.tools.util.StringUtil;
import com.github.pister.dbsync.endpoint.remoting.Request;
import com.github.pister.dbsync.common.tools.util.HessianSerializeUtil;
import com.github.pister.dbsync.common.tools.util.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 用于迁移数据的服务端。
 * Created by songlihuang on 2017/7/13.
 */
public class ServiceExporterServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ServiceExporterServlet.class);

    private ServiceExporter serviceExporter;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String asServerValue = config.getInitParameter("as.server");
        log.warn("as.server:" + asServerValue);
        boolean asServer = Boolean.parseBoolean(asServerValue);
        if (!asServer) {
            return;
        }
        // app1=secret1;app2=secret2;app3=secret3;
        String appSecrets = config.getInitParameter("app.secret");
        if (StringUtil.isEmpty(appSecrets)) {
            throw new RuntimeException("no app secret config, please check init parameters: app.secret in web.xml");
        }
        // 0=user0:pass0@127.0.0.1:3306/db0;1=user1:pass1@127.0.0.1:3306/db1;2=user2:pass2@127.0.0.1:3306/db2
        String dbConfig = config.getInitParameter("db.config");
        if (StringUtil.isEmpty(dbConfig)) {
            throw new RuntimeException("no transfer databases config, please check init parameters: db.config in web.xml");
        }
        serviceExporter = new ServiceExporter(parseAppSecrets(appSecrets));
        parseAndRegisterDbConfig(serviceExporter, dbConfig);
        serviceExporter.init();
    }

    private void parseAndRegisterDbConfig(ServiceExporter serviceExporter, String dbConfig) {
        List<String> parts = StringUtil.splitTrim(dbConfig, ";");
        for (String part : parts) {
            List<String> indexDbConfig = StringUtil.splitTrim(part, "=");
            if (indexDbConfig.size() < 2) {
                throw new IllegalArgumentException("invalidate db.config format:" + part);
            }
            int index = Integer.parseInt(indexDbConfig.get(0));
            String userPassword = StringUtil.getFirstBefore(indexDbConfig.get(1), "@");
            String url = StringUtil.getFirstAfter(indexDbConfig.get(1), "@");
            List<String> userPasswordPart = StringUtil.splitTrim(userPassword, ":");
            if (userPasswordPart.size() < 2) {
                throw new IllegalArgumentException("invalidate db.config format:" + part);
            }
            serviceExporter.registerDbConfig(index, url, userPasswordPart.get(0), userPasswordPart.get(1));
            log.warn("db.config register index: " + index + ", url: " + url + ", user: " + userPasswordPart.get(0));
        }
    }

    private Map<String, String> parseAppSecrets(String appSecrets) {
        Map<String, String> ret = MapUtil.newHashMap();
        List<String> parts = StringUtil.splitTrim(appSecrets, ";");
        for (String part : parts) {
            List<String> kv = StringUtil.splitTrim(part, "=");
            if (kv.size() < 2) {
                throw new IllegalArgumentException("invalidate app.secret format:" + part);
            }
            ret.put(kv.get(0), kv.get(1));
            log.warn("app.secret register appKey:" + kv.get(0));
        }
        return ret;
    }

    protected void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        InputStream inputStream = req.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IoUtil.copy(inputStream, bos);
        byte[] requestBytes = bos.toByteArray();
        Request request = (Request) HessianSerializeUtil.toObject(requestBytes);
        Response response = serviceExporter.invoke(request);
        byte[] responseBytes = HessianSerializeUtil.toBytes(response);
        OutputStream os = resp.getOutputStream();
        try {
            os.write(responseBytes);
        } finally {
            IoUtil.close(os);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        execute(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        execute(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        execute(req, resp);
    }
}
