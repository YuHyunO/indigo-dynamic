package mb.dnm.service;

import mb.dnm.core.Service;
public abstract class AbstractService implements Service {
    protected boolean ignoreError = false;
    protected boolean exceptionHandlingMode = false;
    protected String description;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
    }

    @Override
    public boolean isIgnoreError() {
        return ignoreError;
    }

    public void setExceptionHandlingMode(boolean exceptionHandlingMode) {
        this.exceptionHandlingMode = exceptionHandlingMode;
    }

    public boolean isExceptionHandlingMode() {
        return exceptionHandlingMode;
    }



}
