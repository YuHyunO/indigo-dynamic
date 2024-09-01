package com.mb.mapper;

import java.util.Map;

public class KeyMapper extends Mapper {
    private String mapperId;
    private boolean createNullKey = true;
    private Map<String, String> map;


    @Override
    public Object getMapped(String toMapped) {
        return null;
    }
}
