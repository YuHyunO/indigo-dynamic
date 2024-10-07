package com.mb.util;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.util.MessageUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MessageUtilTest {

    @Test
    public void toStringBufTest() throws Exception {
        Exception e = new Exception("An exception occurred");
        IllegalStateException ia = new IllegalStateException(e);
        IllegalArgumentException iaa = new IllegalArgumentException(ia);

        Map<String, Object> errors = new HashMap<>();
        errors.put("message", MessageUtil.toString(iaa));
        //System.out.println(MessageUtil.mapToJson(errors, true));
        System.out.println(errors.get("message"));
        //System.out.println(MessageUtil.toStringBuf(iaa));
    }
}
