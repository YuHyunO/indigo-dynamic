package com.mb.json;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.util.MessageUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StringToJsonTest {

    @Test
    public void json_test() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        map.put("e", "5");
        map.put("f", "6");
        map.put("g", "7");
        map.put("h", "8");
        map.put("i", "9");
        map.put("j", "10");
        map.put("k", "11");
        map.put("l", "12");
        map.put("m", "13");
        map.put("n", "14");
        map.put("o", "15");
        map.put("p", "16");
        map.put("q", "17");


        log.info("{}", MessageUtil.mapToJson(map, false));
    }
}
