package com.mb.filter;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileNamePatternFilter;
import org.junit.Test;

@Slf4j
public class FileNameFilterTest {

    @Test
    public void fileNameFilter_test() {
        log.info("{}", new FileNamePatternFilter("abc*.xml"));
        log.info("{}", new FileNamePatternFilter("Cyprus.*"));
        log.info("{}", new FileNamePatternFilter("*"));
        log.info("{}", new FileNamePatternFilter("asdasd"));
    }

    @Test
    public void fileNameFilter_accept_test() {
        log.info("{}", new FileNamePatternFilter("abc*.xml").accept("abcdefg.xml"));
        log.info("{}", new FileNamePatternFilter("abc*.xml").accept("abscdefg.xml"));
        log.info("{}", new FileNamePatternFilter("abc*.xml").accept("abc.xmsl"));
        log.info("{}", new FileNamePatternFilter("Cyprus.*").accept("Cyprus.txt"));
        log.info("{}", new FileNamePatternFilter("Cyprus.*").accept("Cypruss.txt"));
        log.info("{}", new FileNamePatternFilter("*").accept("newton.xlsx"));
        log.info("{}", new FileNamePatternFilter("*").accept("제주도.png"));
        log.info("{}", new FileNamePatternFilter("*").accept("제주도"));
        log.info("{}", new FileNamePatternFilter("asdasd").accept("asdasd"));
        log.info("{}", new FileNamePatternFilter("asdasd").accept("asdasd.txt"));
    }

}
