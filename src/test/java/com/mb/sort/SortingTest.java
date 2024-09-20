package com.mb.sort;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.util.SortingUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SortingTest {

    @Test
    public void sort_test() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        list.add("e");
        list.add("f");
        list.add("g");
        list.add("h");
        list.add("i");

        SortingUtil.sortList(list, SortingUtil.Sorting.DESC);


        for (String str : list) {
            log.info(str);
        }
        SortingUtil.sortList(list, SortingUtil.Sorting.ASC);

        for (String str : list) {
            log.info(str);
        }
    }
}
