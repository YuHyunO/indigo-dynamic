package mb.dnm.exeption.handler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.Service;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.AbstractService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Setter
public class ServiceProcessableErrorHandler extends AbstractErrorHandler{
    private List<Service> services;
    private boolean ignoreException = false;

    @Override
    public void handleError(ServiceContext ctx) throws Throwable {
        String txId = ctx.getTxId();
        log.warn("[{}]Start error handler chaining", txId);
        for (Service service : services) {
            Class clazz = service.getClass();
            try {
                log.info("[{}]ERROR-HANDLER-CHAINING: starts the service '{}'", txId, clazz);
                service.process(ctx);
            } catch (Throwable t) {
                if (!ignoreException) {
                    throw t;
                } else {
                    log.warn("[{}]An exception occurred during error handling but was ignored. Exception message: {}", txId, t.getMessage());
                }
            }
        }

    }

    public void setServices(List<Service> services) {
        List<Service> newServices = new ArrayList<>();
        for (Service service : services) {
            if (AbstractService.class.isAssignableFrom(service.getClass())) {
                ((AbstractService) service).setExceptionHandlingMode(true);
            }
            newServices.add(service);
        }

    }

}
