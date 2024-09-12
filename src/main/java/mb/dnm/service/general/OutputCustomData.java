package mb.dnm.service.general;

import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

public class OutputCustomData extends ParameterAssignableService {
    private Object customData = null;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        setOutputValue(ctx, customData);
    }

    public void setCustomData(Object customData) {
        this.customData = customData;
    }

}
