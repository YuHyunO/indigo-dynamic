package mb.dnm.service.test;

import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

import java.util.HashMap;
import java.util.Map;

public class OutputDummyData extends ParameterAssignableService {
    private Map<String, Object> dummyData = new HashMap<>();

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (dummyData.isEmpty()) {
            dummyData.put("KEY1", "VAL1");
            dummyData.put("KEY2", "VAL2");
            dummyData.put("KEY3", "VAL3");
            dummyData.put("KEY4", "VAL4");
            dummyData.put("KEY5", "VAL5");
            dummyData.put("KEY6", "VAL6");
            dummyData.put("KEY7", "VAL7");
        }
        setOutputValue(ctx, dummyData);
    }

    public void setDummyData(Map<String, Object> dummyData) {
        this.dummyData = dummyData;
    }

}
