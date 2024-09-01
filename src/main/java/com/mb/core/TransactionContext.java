package com.mb.core;

import com.mb.code.ProcessCode;
import com.mb.storage.InterfaceInfo;
import com.mb.util.MessageUtil;
import com.mb.util.TxIdGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class TransactionContext {
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


    public TransactionContext(InterfaceInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("InterfaceInfo is null");
        }
        this.info = info;
        startTime = new Date();
        txId = TxIdGenerator.generateTxId(info.getInterfaceId(), startTime);
        serviceTrace = new ArrayList<>();
        errorTrace = new LinkedHashMap<>();
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
}
