package com.mb.mapper;

import mb.dnm.util.StringFunction;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class StringFunctionTest {

    @Test
    public void substring_test() {
        String test = "abcdefg& 100092";
        String result = new StringFunction().substring(test, 2, 5);
        log.info(">>{}", result);
    }

    @Test
    public void length_test() {
        String test = "abc";
        int result = new StringFunction().length(test);
        log.info(">>{}", result);
    }

    @Test
    public void concat_test_1() {
        String str1 = "abc";
        String delimiter = "&";
        String str2 = "def";

        String result = str1.concat(delimiter).concat(str2);
        log.info(">>{}", result);
    }

    @Test
    public void build_test() {
        String delimiter = "$";
        String result = new StringFunction().build(delimiter, "ABC", "가나다", "1239999", 111222);

        log.info("{}", result);
    }

    @Test
    public void ltrim_test() {
        String space = "     ";
        String test = space + "abcsss   ";
        log.info("space length: {}", space.length());
        log.info("test length: {}", test.length());
        String result = new StringFunction().ltrim(test);
        log.info("{}", result);
        log.info("result length: {}", result.length());
    }

    @Test
    public void rtrim_test() {
        String space = ";     ";
        String test = space + "abcsss     ";
        log.info("space length: {}", space.length());
        log.info("test length: {}", test.length());
        String result = new StringFunction().rtrim(test);
        log.info("{}", result);
        log.info("result length: {}", result.length());
    }

    @Test
    public void rpad() {
        String test = "abc";

        log.info("original: {}, len: {}", test, test.length());
        log.info("rpad(test, 7, \"*\"): {}, len: {}", new StringFunction().rpad(test, 7, "*"), new StringFunction().rpad(test, 7, "*").length());
        log.info("rpad(test, 7): {}, len: {}", new StringFunction().rpad(test, 7), new StringFunction().rpad(test, 7, "*").length());
    }

    @Test
    public void lpad() {
        String test = "abc";
        log.info("test: {}", new StringFunction().lpad(test, 7, "*"));
        log.info("test: {}", new StringFunction().lpad(test, 7));
    }
}
