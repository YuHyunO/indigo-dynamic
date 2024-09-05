package mb.dnm.service.test;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

@Slf4j
public class PrintServiceInput extends ParameterAssignableService {

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        Object input = getInputValue(ctx);
        log.info("{}", input.toString());
    }


}
