package com.mb.mapper;

import java.util.Map;

public class KeyMapper extends Mapper {
    private boolean createNullKey = true;
    private Map<String, String> map;

    @Override
    public Object getMapped(String toMapped) {
        return null;
    }

    public void setCreateNullKey(boolean createNullKey) {
        this.createNullKey = createNullKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("KeyMapper{");
        sb.append("id='").append(id).append('\'');
        sb.append(", create_null_key=").append(createNullKey);
        sb.append(", map=").append(map);
        sb.append('}');
        return sb.toString();
    }
}
