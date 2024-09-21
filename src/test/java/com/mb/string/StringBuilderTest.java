package com.mb.string;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class StringBuilderTest {

    @Test
    public void charSequence_test() {
        StringBuilder bd = new StringBuilder("ABCDEFGHIJKLMNOP\n가나다라마바사");
        int idx = bd.indexOf("\n");
        CharSequence sequence = bd.subSequence(0, idx);
        log.info("{}/{}", sequence.toString(), sequence.toString().contains("\n"));
    }
}
