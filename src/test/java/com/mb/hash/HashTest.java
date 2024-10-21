package com.mb.hash;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;

@Slf4j
public class HashTest {

    @Test
    public void hashcode_test() {
        String str1 = "IF001-114";
        String str2 = "IF001-4412";
        String str3 = "IF001-4472";
        String str4 = "IF001-4474";
        String str5 = "IF001-4478";
        String str6 = "IF001-4491";
        String str7 = "IF001-4490";

        log.info("{}", str1.hashCode());
        log.info("{}", str2.hashCode());
        log.info("{}", str3.hashCode());
        log.info("{}", str4.hashCode());
        log.info("{}", str5.hashCode());
        log.info("{}", str6.hashCode());
        log.info("{}", str7.hashCode());
        new HashMap<>().put("", "");
    }
}
