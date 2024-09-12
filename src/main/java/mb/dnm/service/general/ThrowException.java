package mb.dnm.service.general;

import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.AbstractService;

public class ThrowException extends AbstractService {
    private String msg = "ThrowException service throws an exception";

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        throw new Exception(msg);
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
