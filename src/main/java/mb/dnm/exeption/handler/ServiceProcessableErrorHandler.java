package mb.dnm.exeption.handler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.Service;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.AbstractService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Service processable error handler.
 */
@Slf4j
@Setter
public class ServiceProcessableErrorHandler extends AbstractErrorHandler implements Serializable {
    private static final long serialVersionUID = 2662458063002485429L;
    private List<Service> services = new ArrayList<>();
    private boolean ignoreException = false;

    @Override
    public void handleError(ServiceContext ctx) throws Throwable {
        String txId = ctx.getTxId();
        log.warn("[{}]Start error handler chaining", txId);
        for (Service service : services) {
            Class clazz = service.getClass();
            try {
                if (ctx.isProcessOn()) {
                    log.warn("[{}]ERROR-HANDLER-CHAINING: starts the service '{}'", txId, clazz);
                    service.process(ctx);
                } else {
                    log.warn("[{}]ERROR-HANDLER-CHAINING: Stop prcess flag is detected. Stopping error handler chaining", txId);
                    break;
                }
            } catch (Throwable t) {
                if (!ignoreException) {
                    throw t;
                } else {
                    log.warn("[{}]An exception occurred during error handling but was ignored. Exception message: {}", txId, t.getMessage());
                }
            }
        }

    }

    /**
     * Sets services.
     *
     * @param services the services
     */
    public void setServices(List<Service> services) {
        for (Service service : services) {
            if (AbstractService.class.isAssignableFrom(service.getClass())) {
                ((AbstractService) service).setExceptionHandlingMode(true);
            }
            this.services.add(service);
        }
    }

}
