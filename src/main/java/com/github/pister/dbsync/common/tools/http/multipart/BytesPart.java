package com.github.pister.dbsync.common.tools.http.multipart;


import com.github.pister.dbsync.common.charset.DefaultCharsets;
import com.github.pister.dbsync.common.tools.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by songlihuang on 2021/7/14.
 */
public class BytesPart extends Part {

    private String fileName;

    private byte[] bytes;

    public BytesPart(String name, String contentType, String fileName, byte[] bytes) {
        this(name, contentType, null, fileName, bytes);
    }

    protected BytesPart(String name, String contentType, String contentTransferEncoding, String fileName, byte[] bytes) {
        super(name, contentType, contentTransferEncoding);
        this.fileName = fileName;
        this.bytes = bytes;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(MultiPartConstants.EXTRA_BYTES);
        outputStream.write(MultiPartConstants.BOUNDARY_BYTES);
        outputStream.write(MultiPartConstants.CRLF_BYTES);

        outputStream.write(MultiPartConstants.CONTENT_DISPOSITION_BYTES);
        outputStream.write(("\"" + name + "\"").getBytes(DefaultCharsets.UTF_8));
        if (StringUtil.isNotEmpty(fileName)) {
            outputStream.write(("; filename=\"" + fileName + "\"").getBytes(DefaultCharsets.UTF_8));
        }
        outputStream.write(MultiPartConstants.CRLF_BYTES);

        if (StringUtil.isNotEmpty(contentType)) {
            outputStream.write((MultiPartConstants.CONTENT_TYPE + contentType).getBytes(DefaultCharsets.UTF_8));
            outputStream.write(MultiPartConstants.CRLF_BYTES);
        }

        if (StringUtil.isNotEmpty(contentTransferEncoding)) {
            outputStream.write((MultiPartConstants.CONTENT_TRANSFER_ENCODING + contentTransferEncoding).getBytes(DefaultCharsets.UTF_8));
            outputStream.write(MultiPartConstants.CRLF_BYTES);
        }

        outputStream.write(MultiPartConstants.CRLF_BYTES);

        outputStream.write(bytes);
        outputStream.write(MultiPartConstants.CRLF_BYTES);

    }
}
