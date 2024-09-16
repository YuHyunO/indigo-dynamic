package mb.dnm.service.general;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.EmptyCheckable;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

import java.util.Collection;

@Slf4j
public class StopIfInputIsNullOrEmpty extends ParameterAssignableService {

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        Object inputVal = getInputValue(ctx);
        if (inputVal == null) {
            ctx.setProcessOn(false);
            log.info("[{}]Input value is null. The service process will be stop", ctx.getTxId());
            return;
        }

        if (inputVal instanceof Collection) {
            if (((Collection) inputVal).isEmpty()) {
                ctx.setProcessOn(false);
                log.info("[{}]Input value is empty. The service process will be stop", ctx.getTxId());
                return;
            }
        }

        if (inputVal instanceof EmptyCheckable) {
            if (((EmptyCheckable) inputVal).isEmpty()) {
                ctx.setProcessOn(false);
                log.info("[{}]Input value is empty. The service process will be stop", ctx.getTxId());
                return;
            }
        }
    }
}
