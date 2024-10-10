package com.mb.cast;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.service.general.OutputCustomData;
import org.apache.commons.beanutils.ConvertUtils;
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

    @Test
    public void array_cast_test() {
        int[] i = {1, 2, 3, 4, 5, 6, 7};
        String[] ss = {"a", "b", "c", "d"};
        Object[] o = (Object[]) ss;
        log.info("len: {}", o.length);
    }

    @Test
    public void cast_test2() throws Exception {
        Object strInt = "7";
        /*Number num = (Number)(ConvertUtils.convert(strInt, Integer.class));
        log.info("num: {}", num);*/

        OutputCustomData service = new OutputCustomData();
        service.setCastType("int");
        service.setCustomData("7");
    }
}
