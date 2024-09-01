package com.mb.mapper;

import java.util.Map;

public class ValueMapper extends Mapper {
    private String mapperId;
    private boolean createNullKey = true;
    private boolean nullToEmptyString = false;
    private Map<String, String> map;


    @Override
    public Object getMapped(String toMapped) {
        return null;
    }
}
