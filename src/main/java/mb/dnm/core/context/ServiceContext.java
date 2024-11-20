package mb.dnm.core.context;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.ClosableStreamWrapper;
import mb.dnm.access.db.QueryMap;
import mb.dnm.code.ProcessCode;
import mb.dnm.core.Service;
import mb.dnm.exeption.ErrorTrace;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;
import mb.dnm.util.TimeUtil;
import mb.dnm.util.TxIdGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Slf4j
@Getter
public class ServiceContext implements Serializable {
    private static final long serialVersionUID = -5482275442360720322L;
    private String txId;
    private InterfaceInfo info;
    private Date startTime;
    private Date endTime;
    @Setter
    private boolean processOn = true;
    private List<Class<? extends Service>> serviceTrace;
    private Map<Integer, InnerServiceTrace> innerServiceTraces;
    private Map<Class<? extends Service>, ErrorTrace> errorTrace;
    private Map<String, Object> contextParams;
    @Setter
    private Map<String, TransactionContext> txContextMap;
    @Setter
    private Map<String, ClosableStreamWrapper> sessionMap;
    private StringBuilder msg;
    @Setter
    private int currentQueryOrder = 0;
    @Setter
    private int currentErrorQueryOrder = 0;
    @Setter
    private int currentDynamicCodeOrder = 0;
    @Setter
    private int currentErrorDynamicCodeOrder = 0;
    @Setter @Getter
    private ProcessCode processStatus = ProcessCode.NOT_STARTED;

    public ServiceContext(InterfaceInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("InterfaceInfo is null");
        }

        this.info = info;
        startTime = new Date();
        txId = TxIdGenerator.generateTxId(info.getInterfaceId(), startTime);
        serviceTrace = new ArrayList<>();
        innerServiceTraces = new LinkedHashMap<>();
        errorTrace = new LinkedHashMap<>();
        contextParams = new HashMap<>();
        txContextMap = new HashMap<>();
        sessionMap = new HashMap<>();
    }

    public void addServiceTrace(Class<? extends Service> service) {
        serviceTrace.add(service);
    }

    public void addServiceTraces(List<Class<? extends Service>> serviceTrace) {
        serviceTrace.addAll(serviceTrace);
    }

    public String getServiceTraceMessage() {
        String msg = "";
        Map<String, Object> msgMap = getServiceTraceMap();
        if (!msgMap.isEmpty()) {
            try {
                msg = MessageUtil.mapToJson(msgMap, true);
            } catch (Exception e) {
            }
        }
        return msg;
    }


    public Map<String, Object> getServiceTraceMap() {
        Map<String, Object> traceMap = new LinkedHashMap<>();

        int i = 1;
        Map<String, Object> innerMap = new LinkedHashMap<>();
        for (Class<? extends Service> serviceClass : serviceTrace) {
            InnerServiceTrace innerTrace = innerServiceTraces.get(i);
            if (innerTrace != null) {
                innerMap.put(String.valueOf(i), serviceClass);
                Map<Integer, InnerServiceTracePair> innerTraceMap = innerTrace.getTraceMap();
                for (Map.Entry<Integer, InnerServiceTracePair> entry : innerTraceMap.entrySet()) {
                    int innerIdx = entry.getKey();
                    InnerServiceTracePair innerTracePair = entry.getValue();
                    innerMap.put(String.valueOf(i) + "-" + innerIdx,
                            "iteration count:" + innerTracePair.getIterationCount()
                                    + ", inner service: " + innerTracePair.getInnerServiceClass());
                }
            } else {
                innerMap.put(String.valueOf(i), serviceClass);
            }

            ++i;
        }

        if (!innerMap.isEmpty()) {
            traceMap.put("service_trace", innerMap);
        }
        return traceMap;
    }



    public void addInnerServiceTrace(int externalServiceIdx, int innerServiceIdx, Class<? extends Service> service) {
        InnerServiceTrace innerTrace = innerServiceTraces.get(externalServiceIdx);
        if (innerTrace == null) {
            innerTrace = new InnerServiceTrace();
            innerTrace.addTrace(innerServiceIdx, service);
            innerServiceTraces.put(externalServiceIdx, innerTrace);
        } else {
            innerTrace.addTrace(innerServiceIdx, service);
        }
    }

    public void addErrorTrace(Class<? extends Service> service, Throwable throwable) {
        errorTrace.put(service, new ErrorTrace(throwable));
    }


    public String getErrorTraceMessage() {
        Map<String, Object> traceMap = getErrorTraceMap();
        String msg = "";
        try {
           msg = MessageUtil.mapToJson(traceMap, true);
        } catch (Exception e) {
            log.error("Exception occurred at getErrorTraceMessage:", e);
        }
        return msg;
    }

    public Map<String, Object> getErrorTraceMap() {
        Map<String, Object> traceMap = new LinkedHashMap<>();
        traceMap.put("tx_id", txId);
        traceMap.put("if_id", getInterfaceId());
        traceMap.put("start_time", startTime);
        traceMap.put("end_time", endTime);
        traceMap.putAll(getServiceTraceMap());
        Map<String, Object> queryHistories = null;
        if (txContextMap.size() > 0) {
            queryHistories = new LinkedHashMap<>();
            for (Map.Entry<String, TransactionContext> entry : txContextMap.entrySet()) {
                TransactionContext txCtx = entry.getValue();
                queryHistories.put(entry.getKey(), txCtx.getQueryHistory());
            }
        }
        if (queryHistories != null) {
            traceMap.put("query_histories", queryHistories);
        }
        traceMap.put("error_trace", errorTrace);
        return traceMap;
    }

    public List<ErrorTrace> getErrorTraces() {
        List<ErrorTrace> errorTraceList = new ArrayList<>();
        for (Map.Entry<Class<? extends Service>, ErrorTrace> entry : errorTrace.entrySet()) {
            errorTraceList.add(entry.getValue());
        }
        return errorTraceList;
    }

    public ErrorTrace getErrorTrace(Class<? extends Service> service) {
        return errorTrace.get(service);
    }

    public String getInterfaceId() {
        return info.getInterfaceId();
    }

    public void stampEndTime() {
        endTime = new Date();
    }

    /**
     * 현재 ServiceContext 에 context 파라미터를 저장한다.
     * */
    public void addContextParam(String key, Object value) {
        contextParams.put(key, value);
    }

    public void addContextParams(Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            this.contextParams.putAll(params);
        }
    }

    public void deleteContextParam(String key) {
        if (contextParams.containsKey(key)) {
            contextParams.remove(key);
        }
    }

    public Object getContextParam(String key) {
        if (key == null)
            return null;
        return contextParams.get(key);
    }

    public Map<String, Object> getContextParams() {
        return contextParams;
    }

    public String nextDynamicCodeId() {
        String[] codeSequence = info.getDynamicCodeSequence();
        if (codeSequence == null) {
            throw new NoSuchElementException("DynamicCode sequence is null");
        }
        int seqSize = codeSequence.length;
        if (currentDynamicCodeOrder >= seqSize)
            throw new NoSuchElementException("DynamicCode sequence reached to the last");
        String code = codeSequence[currentDynamicCodeOrder];
        ++currentDynamicCodeOrder;
        return code;
    }

    public boolean hasMoreDynamicCodes() {
        String[] codeSequence = info.getDynamicCodeSequence();
        int seqSize = codeSequence.length;
        if (currentDynamicCodeOrder < seqSize)
            return true;
        return false;
    }

    public String nextErrorDynamicCodeId() {
        String[] codeSequence = info.getErrorDynamicCodeSequence();
        if (codeSequence == null) {
            throw new NoSuchElementException("ErrorDynamicCode sequence is null");
        }
        int seqSize = codeSequence.length;
        if (currentErrorDynamicCodeOrder >= seqSize)
            throw new NoSuchElementException("ErrorDynamicCode sequence reached to the last");
        String code = codeSequence[currentErrorDynamicCodeOrder];
        ++currentErrorDynamicCodeOrder;
        return code;
    }

    public boolean hasMoreErrorDynamicCodes() {
        String[] codeSequence = info.getErrorDynamicCodeSequence();
        int seqSize = codeSequence.length;
        if (currentErrorDynamicCodeOrder < seqSize)
            return true;
        return false;
    }

    public boolean hasMoreQueryMaps() {
        String[] querySequence = info.getQuerySequence();
        int seqSize = querySequence.length;
        if (currentQueryOrder < seqSize)
            return true;
        return false;
    }

    public QueryMap nextQueryMap() {
        String[] querySequence = info.getQuerySequence();
        if (querySequence == null) {
            throw new NoSuchElementException("Query sequence is null");
        }

        int seqSize = querySequence.length;
        if (currentQueryOrder >= seqSize)
            throw new NoSuchElementException("Query sequence reached to the last");
        String query = querySequence[currentQueryOrder];
        ++currentQueryOrder;

        int executorNameIdx = query.indexOf('$');
        String executorName = query.substring(0, executorNameIdx);
        String queryId = query.substring(executorNameIdx + 1);

        QueryMap qmap = new QueryMap(executorName, queryId);
        qmap.setTimeoutSecond(info.getTxTimeoutSecond());
        addTransactionContext(qmap);

        return new QueryMap(executorName, queryId);
    }

    public boolean hasMoreErrorQueryMaps() {
        String[] querySequence = info.getErrorQuerySequence();
        int seqSize = querySequence.length;
        if (currentErrorQueryOrder < seqSize)
            return true;
        return false;
    }

    public QueryMap nextErrorQueryMap() {
        String[] querySequence = info.getErrorQuerySequence();
        if (querySequence == null) {
            throw new NoSuchElementException("Error query sequence is null");
        }

        int seqSize = querySequence.length;
        if (currentErrorQueryOrder >= seqSize)
            throw new NoSuchElementException("Error query sequence reached to the last");
        String query = querySequence[currentErrorQueryOrder];
        ++currentErrorQueryOrder;

        int executorNameIdx = query.indexOf('$');
        String executorName = query.substring(0, executorNameIdx);
        String queryId = query.substring(executorNameIdx + 1);

        QueryMap qmap = new QueryMap(executorName, queryId);
        qmap.setTimeoutSecond(info.getTxTimeoutSecond());
        addTransactionContext(qmap);

        return new QueryMap(executorName, queryId);
    }

    void addTransactionContext(QueryMap queryMap) {
        String executorName = queryMap.getExecutorName();
        TransactionContext txCtx = null;
        if (!txContextMap.containsKey(executorName)) {
            txCtx = new TransactionContext(executorName);
            txCtx.setTimeoutSecond(queryMap.getTimeoutSecond());
            txCtx.addQueryHistory(queryMap.getQueryId());
            txContextMap.put(executorName, txCtx);
        } else {
            txContextMap.get(executorName).addQueryHistory(queryMap.getQueryId());
        }
    }

    /**
     * @return 'true' if the TransactionContext is newly created. 'false' if the TransactionContext is already exist.
     * <br>
     * But the group transaction property is always enabled in both cases.
     * */
    public boolean setGroupTransaction(String executorName, boolean groupTxEnabled) {
        TransactionContext txCtx = txContextMap.get(executorName);
        if (txCtx == null) {
            txCtx = new TransactionContext(executorName);
            txCtx.setGroupTxEnabled(groupTxEnabled);
            txContextMap.put(executorName, txCtx);
            return true;
        } else {
            txCtx.setGroupTxEnabled(groupTxEnabled);
            return false;
        }
    }

    public TransactionContext getTransactionContext(QueryMap queryMap) {
        return txContextMap.get(queryMap.getExecutorName());
    }

    public TransactionContext getTransactionContext(String executorName) {
        return txContextMap.get(executorName);
    }

    public Map<String, TransactionContext> getTransactionContextMap() {
        return txContextMap;
    }

    public void addSession(String sourceName, ClosableStreamWrapper session) {
        sessionMap.put(sourceName, session);
    }

    public ClosableStreamWrapper getSession(String sourceName) {
        return sessionMap.get(sourceName);
    }

    public boolean isSessionExist(String sourceName) {
        return sessionMap.containsKey(sourceName);
    }

    public Map<String, ClosableStreamWrapper> getSessionMap() {
        return sessionMap;
    }

    public boolean isErrorExist() {
        if (!errorTrace.isEmpty()) {
            return true;
        }
        return false;
    }

    public void setMsg(StringBuilder msg) {
        this.msg = msg;
    }

    public void setMsg(String msg) {
        this.msg = new StringBuilder(msg);
    }


    public String getMsg() {
        if (msg == null) {
            return null;
        }
        return msg.toString();
    }

    public Map<String, Object> getContextInformation() {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("$tx_id", txId);
        infoMap.put("$if_id", getInterfaceId());
        infoMap.put("$tx_msg", getMsg());
        infoMap.put("$process_status", processStatus.getProcessCode());
        infoMap.put("$start_time_timestamp", TimeUtil.getFormattedTime(startTime, TimeUtil.TIMESTAMP_FORMAT));
        infoMap.put("$start_time_date", TimeUtil.getFormattedTime(startTime, TimeUtil.DATETIME_FORMAT));
        infoMap.put("$end_time_timestamp", TimeUtil.getFormattedTime(endTime, TimeUtil.TIMESTAMP_FORMAT));
        infoMap.put("$end_time_date", TimeUtil.getFormattedTime(endTime, TimeUtil.DATETIME_FORMAT));
        infoMap.put("$YYYY", TimeUtil.curDate(TimeUtil.YYYY));
        infoMap.put("$YYYYMM", TimeUtil.curDate(TimeUtil.YYYYMM));
        infoMap.put("$YYYYMMDD", TimeUtil.curDate(TimeUtil.YYYYMMDD));
        infoMap.put("$YYYYMMDDHHmmss", TimeUtil.curDate(TimeUtil.YYYYMMDDHHmmss));
        return infoMap;
    }

    public class InnerServiceTrace implements Serializable {
        private static final long serialVersionUID = -7111196943824771103L;
        @Getter
        private Map<Integer, InnerServiceTracePair> traceMap;

        public InnerServiceTrace() {
            traceMap = new LinkedHashMap<>();
        }

        public void addTrace(int innerServiceIdx, Class<? extends Service> serviceClass) {
            InnerServiceTracePair tracePair = traceMap.get(innerServiceIdx);
            if (tracePair == null) {
                tracePair = new InnerServiceTracePair(serviceClass);
                tracePair.stampTrace(serviceClass);
                traceMap.put(innerServiceIdx, tracePair);
            } else {
                tracePair.stampTrace(serviceClass);
                //traceMap.put(innerServiceIdx, tracePair);
            }
        }

        public InnerServiceTracePair getInnerServiceTrace(int innerServiceIdx) {
            return traceMap.get(innerServiceIdx);
        }

    }

    @Getter
    public class InnerServiceTracePair implements Serializable {
        private static final long serialVersionUID = -8073101364903503521L;
        private final Class<? extends Service> innerServiceClass;
        private int iterationCount = 0;

        public InnerServiceTracePair(Class<? extends Service> innerServiceClass) {
            this.innerServiceClass = innerServiceClass;
        }

        public void stampTrace(Class<? extends Service> innerServiceClass) {
            if (this.innerServiceClass == innerServiceClass) {
                ++iterationCount;
            }
        }

    }

    @Override
    public String toString() {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            return MessageUtil.mapToJson(toMap(), true);
        } catch (Exception e) {
            return super.toString();
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("transactionId", txId);
        map.put("interfaceId", getInterfaceId());
        map.put("interfaceName", info.getInterfaceName());
        map.put("processStatus", processStatus.getProcessCode());
        map.put("serviceId", info.getServiceId());
        map.put("startTime", TimeUtil.getFormattedTime(startTime, TimeUtil.JDBC_TIMESTAMP_FORMAT));
        map.put("endTime", TimeUtil.getFormattedTime(endTime, TimeUtil.JDBC_TIMESTAMP_FORMAT));
        map.put("serviceTrace", getServiceTraceMap());
        if (errorTrace != null && !errorTrace.isEmpty()) {
            map.put("errorTrace", getErrorTrace());
        }
        return map;

    }
}
