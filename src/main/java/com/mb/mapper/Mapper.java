package com.mb.mapper;

public abstract class Mapper {
    protected String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract Object getMapped(String toMapped);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("id=").append(id);
        sb.append("]");
        return sb.toString();
    }
}
