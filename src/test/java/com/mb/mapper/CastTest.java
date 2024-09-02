package com.mb.mapper;

import com.mb.util.CastUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class CastTest {

    @Test
    public void cast_test() {
        CastUtil.simpleTypeCast(Boolean.class, "true");
    }

}
