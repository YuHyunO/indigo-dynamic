package mb.dnm.service.test;

import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.AbstractService;

public class ThrowException extends AbstractService {
    @Override
    public void process(ServiceContext ctx) throws Throwable {
        throw new Exception("This is an exception for test");
    }
}
