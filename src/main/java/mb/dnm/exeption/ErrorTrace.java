package mb.dnm.exeption;

import lombok.Getter;
import mb.dnm.util.MessageUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


public class ErrorTrace implements Serializable {
    private static final long serialVersionUID = -5851150242323943159L;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ErrorTrace{");
        sb.append("message='").append(message).append('\'');
        sb.append(", localizedMessage='").append(localizedMessage).append('\'');
        sb.append(", traceBuffer=").append(traceBuffer);
        sb.append('}');
        return sb.toString();
    }
}
