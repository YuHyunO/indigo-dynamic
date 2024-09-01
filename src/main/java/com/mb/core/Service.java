package com.mb.core;

public interface Service {

    public void process(ServiceContext ctx);

    public void setIgnoreError(boolean ignoreError);

    public boolean isIgnoreError();

}
