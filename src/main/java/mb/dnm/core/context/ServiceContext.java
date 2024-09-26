package mb.dnm.core.context;

import mb.dnm.access.ClosableStreamWrapper;
import mb.dnm.access.db.QueryMap;
import mb.dnm.code.ProcessCode;
import mb.dnm.core.Service;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;
import mb.dnm.util.TxIdGenerator;
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
    private List<Class<? extends Service>> serviceTrace;
    private Map<Class<? extends Service>, Throwable> errorTrace;
    private Map<String, Object> contextParams;
    private Map<String, TransactionContext> txContextMap;
    private Map<String, ClosableStreamWrapper> sessionMap;
    private StringBuilder msg;
    @Setter
    private int currentQueryOrder = 0;
    @Setter
    private int currentErrorQueryOrder = 0;
    private int currentMappingOrder = 0;
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
        errorTrace = new LinkedHashMap<>();
        contextParams = new HashMap<>();
        txContextMap = new HashMap<>();
        sessionMap = new HashMap<>();
    }

    public void addServiceTrace(Class<? extends Service> service) {
        serviceTrace.add(service);
    }

    public String getServiceTraceMessage() {
        Map<String, Object> msgMap = new LinkedHashMap<>();

        int i = 1;
        Map<String, Object> innerMap = new LinkedHashMap<>();
        for (Class<? extends Service> serviceClass : serviceTrace) {
            innerMap.put(String.valueOf(i), serviceClass);
            ++i;
        }

        if (!innerMap.isEmpty()) {
            msgMap.put("service_trace", innerMap);
        }

        String msg = "";
        if (!msgMap.isEmpty()) {
            try {
                msg = MessageUtil.mapToJson(msgMap, true);
            } catch (Exception e) {
            }
        }
        return msg;
    }

    public void addErrorTrace(Class<? extends Service> service, Throwable throwable) {
        errorTrace.put(service, throwable);
    }


    public String getErrorTraceMessage() {
        Map<String, Object> msgMap = new LinkedHashMap<>();
        msgMap.put("tx_id", txId);
        msgMap.put("if_id", getInterfaceId());
        msgMap.put("start_time", startTime);
        msgMap.put("end_time", endTime);
        msgMap.put("service_trace", serviceTrace);
        msgMap.put("error_trace", errorTrace);
        Map<String, Object> queryHistories = null;
        if (txContextMap.size() > 0) {
            queryHistories = new LinkedHashMap<>();
            for (Map.Entry<String, TransactionContext> entry : txContextMap.entrySet()) {
                queryHistories.put(entry.getKey(), entry.getValue());
            }
        }
        if (queryHistories != null) {
            msgMap.put("query_histories", queryHistories);
        }
        String msg = "";
        try {
           msg = MessageUtil.mapToJson(msgMap, true);
        } catch (Exception e) {
        }
        return msg;
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

    public boolean hasMoreQueryMaps() {
        String[] querySequence = info.getQuerySequence();
        int seqSize = querySequence.length;
        if (currentQueryOrder <= seqSize)
            return true;
        return false;
    }

    public QueryMap nextQueryMap() {
        String[] querySequence = info.getQuerySequence();
        if (querySequence == null) {
            throw new NoSuchElementException("Query sequence is null");
        }

        int seqSize = querySequence.length;
        if (currentQueryOrder > seqSize)
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
        if (currentErrorQueryOrder <= seqSize)
            return true;
        return false;
    }

    public QueryMap nextErrorQueryMap() {
        String[] querySequence = info.getErrorQuerySequence();
        if (querySequence == null) {
            throw new NoSuchElementException("Error query sequence is null");
        }

        int seqSize = querySequence.length;
        if (currentErrorQueryOrder > seqSize)
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

    public void setMsg(String msg) {
        this.msg = new StringBuilder(msg);
    }

    public String getMsg() {
        return msg.toString();
    }


}
