package com.mb.mapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class CastTest {

    @Test
    public void cast_test() {
        char c1 = 42;
        char c2 = 63;
        char c3 = 47;

        System.out.println(c1 + ", " + c2 + ", " + c3);
    }

    @Test
    public void determineRootDir() {
        String location = "classpath*:SQL_*.xml";
        int prefixEnd = location.indexOf(":") + 1;

        int rootDirEnd;
        for(rootDirEnd = location.length(); rootDirEnd > prefixEnd && isPattern(location.substring(prefixEnd, rootDirEnd)); rootDirEnd = location.lastIndexOf(47, rootDirEnd - 2) + 1) {
        }

        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }

        System.out.println(location.substring(0, rootDirEnd));
    }

    public boolean isPattern(String path) {
        return path.indexOf(42) != -1 || path.indexOf(63) != -1;
    }
}
