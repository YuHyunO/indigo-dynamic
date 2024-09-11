package mb.dnm.mapper;

import mb.dnm.core.context.ServiceContext;

import java.util.HashMap;
import java.util.Map;

public class ContextPlaceHolderMapper {

    private static final Map<String, String> MAP = new HashMap<>();
    static {
        MAP.put("@{if_id}", "getInterfaceId");
        MAP.put("@{tx_id}", "getTxId");
        MAP.put("@{service_trace}", "getServiceTraceMessage");
        MAP.put("@{error_trace}", "getErrorTraceMessage");
        MAP.put("@{error_trace}", "getErrorTraceMessage");
        MAP.put("@{process_code}", "getProcessStatus");
        MAP.put("@{process_status}", "getProcessStatus");
    }


    public static String toMappedValue(String val, ServiceContext ctx) {

        return null;
    }

}
