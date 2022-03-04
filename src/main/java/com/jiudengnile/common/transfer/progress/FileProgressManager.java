package com.jiudengnile.common.transfer.progress;

import com.github.pister.tson.Tsons;
import wint.lang.utils.FileUtil;
import wint.lang.utils.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class FileProgressManager implements ProgressManager {

    private static final Charset defaultCharset = Charset.forName("utf-8");

    private File baseFile;

    public void init() {
        if (baseFile == null) {
            baseFile = new File(SystemUtil.USER_HOME + "/transfer/client/progress");
        }
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
    }

    @Override
    public void save(String taskName, TableProgress tableProgress) throws IOException {
        File file = new File(baseFile, taskName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        ;
        byte[] data = Tsons.encode(tableProgress).getBytes(defaultCharset);
        FileUtil.writeContent(file, data);
    }

    @Override
    public TableProgress load(String taskName) throws IOException {
        File file = new File(baseFile, taskName);
        if (!file.exists()) {
            return null;
        }
        byte[] data = FileUtil.readContent(file);
        if (data == null) {
            return null;
        }
        return (TableProgress) Tsons.decode(new String(data, defaultCharset));
    }

    public void setBaseFile(String fileName) {
        baseFile = new File(fileName);
    }

}
