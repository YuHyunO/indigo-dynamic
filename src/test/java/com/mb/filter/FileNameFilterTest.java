package com.mb.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Test;

@Slf4j
public class FileNameFilterTest {

    @Test
    public void fileNameFilter_test() {

    }

    @Test
    public void fileNameFilter_accept_test() {
        WildcardFileFilter filter = new WildcardFileFilter("RG0*");
        String fileName = "RG031011001";

        log.info("{}", filter.accept(null, fileName));
    }



}
