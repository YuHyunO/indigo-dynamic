package com.mb.util;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.util.MessageUtil;
import mb.dnm.util.StringUtil;
import mb.dnm.util.TimeUtil;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    @Test
    public void timeUtil_test() {
        String date = TimeUtil.getFormattedTime(new Date(), "yyMMdd");
        log.info("@@ {}", date);

        Map<String, Object> sequence = new HashMap<>();
        Object seq = sequence.get("sequence");
        if (seq == null) {
            seq = "";
        }
        String IFSEQ = new StringBuilder()
                .append(date)
                .append("-")
                .append(StringUtil.lpad(String.valueOf(seq), 7, 0)).toString();
        log.info("@@ {}", IFSEQ);
    }
}
