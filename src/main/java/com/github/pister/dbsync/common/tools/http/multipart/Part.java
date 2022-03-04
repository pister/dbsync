package com.github.pister.dbsync.common.tools.http.multipart;

import java.io.IOException;
import java.io.OutputStream;

public abstract class Part {

    protected String name;

    protected String contentType;

    protected String contentTransferEncoding;

    public Part(String name, String contentType, String contentTransferEncoding) {
        this.name = name;
        this.contentType = contentType;
        this.contentTransferEncoding = contentTransferEncoding;
    }

    public abstract void writeTo(OutputStream outputStream) throws IOException;

}
