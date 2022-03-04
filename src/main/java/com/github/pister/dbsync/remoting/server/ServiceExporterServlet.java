package com.github.pister.dbsync.remoting.server;

import com.github.pister.dbsync.remoting.Response;
import com.github.pister.dbsync.util.StringUtil;
import com.github.pister.dbsync.remoting.Request;
import com.github.pister.dbsync.util.HessianSerializeUtil;
import com.github.pister.dbsync.util.IoUtil;
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

/**
 * 用于迁移数据的服务端。
 * Created by songlihuang on 2017/7/13.
 */
public class ServiceExporterServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ServiceExporterServlet.class);

    private ServiceExporter serviceExporter = new ServiceExporter();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        boolean asServer = Boolean.getBoolean(config.getInitParameter("xdata.transfer.server"));
        config.getServletContext().log("asServer:" + asServer);
        if (!asServer) {
            return;
        }
        String databases = config.getInitParameter("xdata.databases.transfer.config");
        if (StringUtil.isEmpty(databases)) {
            throw new RuntimeException("no transfer databases config, please check init parameters: xdata.databases.transfer.config in web.xml");
        }
        serviceExporter.setDatabases(databases);
        serviceExporter.init();
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
