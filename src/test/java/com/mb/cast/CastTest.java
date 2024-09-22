package com.mb.cast;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.*;

@Slf4j
public class CastTest {

    @Test
    public void cast_test() {
        Object test = "asda";
        //Map <String, Object> map = (Map<String, Object>) test;

        String[] strarr = new String[]{"asda", "asda"};
        List<String> strlist = new ArrayList<String>();
        System.out.println(strarr.getClass().isAssignableFrom(Iterator.class));
        System.out.println(strlist.getClass().isAssignableFrom(Iterator.class));
        System.out.println(Arrays.class.isAssignableFrom(strarr.getClass()));
        System.out.println(Iterable.class.isAssignableFrom(strlist.getClass()));

    }
}
