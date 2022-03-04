package com.jiudengnile.common.transfer.util;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import wint.lang.io.FastByteArrayInputStream;
import wint.lang.io.FastByteArrayOutputStream;

import java.io.IOException;

/**
 * User: huangsongli
 * Date: 16/9/30
 * Time: 下午2:51
 */
public final class HessianSerializeUtil {

    private HessianSerializeUtil() {
    }

    public static byte[] toBytes(Object input) {
        if (input == null) {
            return null;
        }
        FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
        HessianOutput hessianOutput = new HessianOutput(bos);
        try {
            hessianOutput.writeObject(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    public static Object toObject(byte[] data) {
        if (data == null) {
            return null;
        }
        FastByteArrayInputStream bis = new FastByteArrayInputStream(data);
        HessianInput hessianInput = new HessianInput(bis);
        try {
            return hessianInput.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
