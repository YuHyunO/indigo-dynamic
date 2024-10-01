package mb.dnm.core.dynamic;

import mb.dnm.core.context.ServiceContext;

public interface DynamicCode {
    public void execute(ServiceContext ctx) throws Throwable;
}
