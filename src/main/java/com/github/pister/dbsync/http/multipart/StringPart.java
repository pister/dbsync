package com.github.pister.dbsync.http.multipart;

import java.nio.charset.Charset;

/**
 * Created by songlihuang on 2021/7/14.
 */
public class StringPart extends BytesPart {

    public StringPart(String name, String value) {
        super(name, null, "utf-8", null, value.getBytes(Charset.defaultCharset()));
    }
}
