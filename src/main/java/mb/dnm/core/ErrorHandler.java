package mb.dnm.core;

import mb.dnm.core.context.ServiceContext;

public interface ErrorHandler {

    public boolean isTriggered(Class<? extends Throwable> clazz);

    public void handleError(ServiceContext ctx);



}

