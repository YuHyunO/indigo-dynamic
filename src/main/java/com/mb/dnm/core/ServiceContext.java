package com.mb.dnm.core;

import com.mb.dnm.code.ProcessCode;
import com.mb.dnm.storage.InterfaceInfo;
import com.mb.dnm.util.MessageUtil;
import com.mb.dnm.util.TxIdGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class ServiceContext {
    private String txId;
    private InterfaceInfo info;
    private Date startTime;
    private Date endTime;
    @Setter
    private boolean processOn = true;
    @Setter
    private ProcessCode processCode = ProcessCode.NOT_STARTED;
    @Setter
    private Throwable error;
    private List<Class<? extends Service>> serviceTrace;
    private Map<Class<? extends Service>, Throwable> errorTrace;
    private Map<String, Object> contextParams;

    private int currentQueryOrder = 0;
    private int currentMappingOrder = 0;


    public ServiceContext(InterfaceInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("InterfaceInfo is null");
        }
        this.info = info;
        startTime = new Date();
        txId = TxIdGenerator.generateTxId(info.getInterfaceId(), startTime);
        serviceTrace = new ArrayList<>();
        errorTrace = new LinkedHashMap<>();
        contextParams = new HashMap<>();
    }

    public void addServiceTrace(Class<? extends Service> service) {
        serviceTrace.add(service);
    }

    public String getServiceTraceMessage() {
        StringBuilder sb = new StringBuilder();
        for (Class service : serviceTrace) {
            sb.append(service.getName());
            sb.append("→");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public void addErrorTrace(Class<? extends Service> service, Throwable throwable) {
        errorTrace.put(service, throwable);
    }

    public String getErrorTraceMessage() {
        StringBuilder sb = new StringBuilder();
        for (Class service : errorTrace.keySet()) {
            Throwable throwable = errorTrace.get(service);
            sb.append("Error class: ");
            sb.append(service.getName());
            sb.append("\n");
            sb.append("Error trace: ");
            sb.append(MessageUtil.toStringBuf(throwable));
            sb.append("\n");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public String getInterfaceId() {
        return info.getInterfaceId();
    }

    public void stampEndTime() {
        endTime = new Date();
    }

    public void addContextParam(String key, Object value) {
        contextParams.put(key, value);
    }

    public Object getContextParam(String key) {
        if (key == null)
            return null;
        return contextParams.get(key);
    }

    public String getCurrentQuery() {
        String[] querySequence = info.getQuerySequence();
        if (querySequence == null) {
            return null;
        }
        int seqSize = querySequence.length;

        return null;
    }

    public String getQueryExecutorName() {
        return null;
    }

}
