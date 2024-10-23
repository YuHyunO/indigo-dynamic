package com.mb.map;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MapAllocateTest {

    @Test
    public void testMapAllocate() {
        Map<String, Object> map = new HashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);


        Map<String, Object> context = new HashMap<>();
        context.put("master", map);

        Map<String, Object> copy = (Map<String, Object>) context.get("master");
        copy.put("D", 4);

        context.put("sub", copy);

        log.info("{}", context.get("master").equals(context.get("sub"))); //true
        log.info("{}", context.get("master"));
        log.info("{}", context.get("sub"));

    }
}
