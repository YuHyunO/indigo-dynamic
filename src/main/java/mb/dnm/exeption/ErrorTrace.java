package mb.dnm.exeption;

import mb.dnm.util.MessageUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorTrace implements Serializable {
    private String message;
    private String localizedMessage;
    private StringBuffer traceBuffer;


    public ErrorTrace(Throwable t) {
        traceBuffer = MessageUtil.toStringBuf(t);
        message = t.getMessage();
        localizedMessage = t.getLocalizedMessage();
    }

    public Map<String, Object> getTraceMap() {
        Map<String, Object> traceMap = new LinkedHashMap<>();
        traceMap.put("message", message);
        traceMap.put("localizedMessage", localizedMessage);
        traceMap.put("stackTrace", traceBuffer.toString());
        return traceMap;
    }



}
