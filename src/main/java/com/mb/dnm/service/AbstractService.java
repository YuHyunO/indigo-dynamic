package com.mb.dnm.service;

import com.mb.dnm.core.Service;
public abstract class AbstractService implements Service {
    protected boolean ignoreError = false;

    @Override
    public void setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
    }

    @Override
    public boolean isIgnoreError() {
        return ignoreError;
    }

}
