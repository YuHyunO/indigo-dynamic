package mb.dnm.exeption.handler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.Service;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.AbstractService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExecuteServiceErrorHandler extends AbstractErrorHandler implements Serializable {
    private static final long serialVersionUID = 6590938203097560200L;
    private Service service;

    @Override
    public void handleError(ServiceContext ctx) throws Throwable {
        if (service == null) {
            throw new IllegalStateException("Service class for error handler is null");
        }
        log.warn("[{}]Start error handler chaining", ctx.getTxId());
        service.process(ctx);

    }

    public void setService(Service service) {
        if (AbstractService.class.isAssignableFrom(service.getClass())) {
            ((AbstractService) service).setExceptionHandlingMode(true);
        }
        this.service = service;
    }

}
