package com.mb.dnm.core;

import com.mb.dnm.core.context.ServiceContext;

public interface Service {

    public void process(ServiceContext ctx) throws Throwable;

    public void setIgnoreError(boolean ignoreError);

    public boolean isIgnoreError();

}
