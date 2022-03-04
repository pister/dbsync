package com.jiudengnile.common.transfer.http.multipart;


import com.jiudengnile.common.transfer.charset.DefaultCharsets;

/**
 * Created by songlihuang on 2021/7/14.
 */
public interface MultiPartConstants {

    String BOUNDARY = "-----RP2021071431459";

    byte[] BOUNDARY_BYTES = BOUNDARY.getBytes(DefaultCharsets.UTF_8);

    /**
     * Carriage return/linefeed
     */
    String CRLF = "\r\n";

    /**
     * Carriage return/linefeed as a byte array
     */
    byte[] CRLF_BYTES = CRLF.getBytes(DefaultCharsets.UTF_8);

    /**
     * Content dispostion characters
     */
    String QUOTE = "\"";

    /**
     * Content dispostion as a byte array
     */
    byte[] QUOTE_BYTES = QUOTE.getBytes(DefaultCharsets.UTF_8);

    /**
     * Extra characters
     */
    String EXTRA = "--";

    /**
     * Extra characters as a byte array
     */
    byte[] EXTRA_BYTES = EXTRA.getBytes(DefaultCharsets.UTF_8);

    /**
     * Content dispostion characters
     */
    String CONTENT_DISPOSITION = "Content-Disposition: form-data; name=";

    /**
     * Content dispostion as a byte array
     */
    byte[] CONTENT_DISPOSITION_BYTES = CONTENT_DISPOSITION.getBytes(DefaultCharsets.UTF_8);

    /**
     * Content type header
     */
    String CONTENT_TYPE = "Content-Type: ";

    String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: ";

    byte[] CONTENT_TRANSFER_ENCODING_BYTES = CONTENT_TRANSFER_ENCODING.getBytes(DefaultCharsets.UTF_8);


}
