package mb.dnm.core;

import mb.dnm.core.context.ServiceContext;

public interface Service {

    public void process(ServiceContext ctx) throws Throwable;

    public void setIgnoreError(boolean ignoreError);

    public boolean isIgnoreError();

}
