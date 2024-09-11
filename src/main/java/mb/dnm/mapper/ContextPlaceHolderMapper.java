package mb.dnm.mapper;

import mb.dnm.core.context.ServiceContext;

import java.util.HashMap;
import java.util.Map;

public class ContextPlaceHolderMapper {

    private static final Map<String, String> MAP = new HashMap<>();
    static {
        MAP.put("@{if_id}", "");
        MAP.put("@{tx_id}", "");
    }


    public static String replacePlaceHolder(String val, ServiceContext ctx) {
        return null;
    }

}
