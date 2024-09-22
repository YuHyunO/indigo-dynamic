package com.mb.calc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class Calc {

    @Test
    public void remain() {
        int fetchSize = 3;
        int fetched = 3;

        log.info("{}", fetchSize % fetched);
    }

}
