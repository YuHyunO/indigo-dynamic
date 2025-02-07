package mb.dnm.exeption;

import lombok.Getter;
import mb.dnm.util.MessageUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * The type Error trace.
 */
public class ErrorTrace implements Serializable {
    private static final long serialVersionUID = -5851150242323943159L;
    @Getter
    private String message;
    @Getter
    private String localizedMessage;
    @Getter
    private StringBuffer traceBuffer;


    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets localized message.
     *
     * @return the localized message
     */
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    /**
     * Sets localized message.
     *
     * @param localizedMessage the localized message
     */
    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
    }

    /**
     * Gets trace buffer.
     *
     * @return the trace buffer
     */
    public StringBuffer getTraceBuffer() {
        return traceBuffer;
    }

    /**
     * Sets trace buffer.
     *
     * @param traceBuffer the trace buffer
     */
    public void setTraceBuffer(StringBuffer traceBuffer) {
        this.traceBuffer = traceBuffer;
    }

    /**
     * Instantiates a new Error trace.
     *
     * @param t the t
     */
    public ErrorTrace(Throwable t) {
        traceBuffer = MessageUtil.toStringBuf(t);
        message = t.getMessage();
        localizedMessage = t.getLocalizedMessage();
    }

    /**
     * Gets trace map.
     *
     * @return the trace map
     */
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
