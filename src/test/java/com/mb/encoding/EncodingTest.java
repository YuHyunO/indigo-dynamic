package com.mb.encoding;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Map;

@Slf4j
public class EncodingTest {

    @Test
    public void ISO8859_1_test() throws Exception {
        String encoding = "ISO-8859-1";
        String korStr = "메추라기";
        byte[] ba1 = korStr.getBytes(encoding);
        byte[] ba2 = korStr.getBytes("UTF-8");
        byte[] ba3 = korStr.getBytes("EUC-KR");

        log.info("ISO-8859-1: {} / {}", ba1, new String(ba1, encoding));
        log.info("UTF-8: {} / {}", ba2, new String(ba2, "UTF-8"));
        log.info("EUC-KR: {} / {}", ba3, new String(ba3, "EUC-KR"));
    }

    @Test
    public void available_charset_test() {
        Map<String, Charset> map = Charset.availableCharsets();
        Charset.forName("ms949");

        for (Map.Entry<String, Charset> entry : map.entrySet()) {
            log.info("{}  ---  {}", entry.getKey(), entry.getValue());
        }
    }
}
