package com.mb.cast;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
public class TypeTest {

    @Test
    public void type_test() {

        Object obj = new int[3];
        String type = obj.getClass().getName();

        log.info(type);
        log.info("{}", obj.getClass().isArray());

        Object obj2 = "asd";
        Class type2 = obj2.getClass();

        log.info("type3: {}", type2);
        log.info("type2: {}", CharSequence.class.isAssignableFrom(type2));

        Object obj3 = new ArrayList<>();
        Class type3 = obj3.getClass();

        log.info("type3: {}", type3);
        log.info("type3: {}", Collection.class.isAssignableFrom(type3));

        Number num = new Integer(3);
        log.info("num: {}", num);
        log.info("num: {}", num.getClass());

        Character ch = new Character('a');
        log.info("ch: {}", ch.charValue());
    }
}
