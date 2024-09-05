package mb.dnm.core.callback;

import mb.dnm.core.context.ServiceContext;

public interface AfterProcessListener {

    public void afterProcess(ServiceContext ctx);

}
