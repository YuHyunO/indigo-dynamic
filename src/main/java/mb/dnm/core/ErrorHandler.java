package mb.dnm.core;

import mb.dnm.core.context.ServiceContext;

public interface ErrorHandler {

    public void handleError(ServiceContext ctx);

}

