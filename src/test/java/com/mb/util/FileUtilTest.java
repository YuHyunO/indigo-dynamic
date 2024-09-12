package com.mb.util;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.util.FileUtil;
import org.junit.Test;

@Slf4j
public class FileUtilTest {

    @Test
    public void removeLastPathSeparator_test() {
        String path = "/Users/mb/Desktop/test.txt////";
        String path2 = "\\Users\\mb\\Desktop\\test.txt\\";
        String path3 = "\\Users\\mb\\Desktop\\test.txt\\\\\\\\\\\\";

        log.info("{}", FileUtil.removeLastPathSeparator(path));
        log.info("{}", FileUtil.removeLastPathSeparator(path2));
        log.info("{}", FileUtil.removeLastPathSeparator(path3));

    }

    @Test
    public void supposeFileSeparator_test() {
        String path = "/Users/mb/Desktop/test.txt////";
        String path2 = "\\Users\\mb\\Desktop\\test.txt\\";
        String path3 = "\\Users\\/mb\\Desktop\\test.txt\\";

        log.info("{}", FileUtil.supposeFileSeparator(path));
        log.info("{}", FileUtil.supposeFileSeparator(path2));
        log.info("{}", FileUtil.supposeFileSeparator(path3));
    }
}
