package mb.dnm.core.context;

import mb.dnm.access.ClosableSession;
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
    @Setter
    private ProcessCode processCode = ProcessCode.NOT_STARTED;
    private List<Class<? extends Service>> serviceTrace;
    private Map<Class<? extends Service>, Throwable> errorTrace;
    private Map<String, Object> contextParams;
    private Map<String, TransactionContext> txContextMap;
    private Map<String, ClosableSession> sessionMap;

    private int currentQueryOrder = 0;
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

    public void addSession(String sourceName, ClosableSession session) {
        sessionMap.put(sourceName, session);
    }

    public ClosableSession getSession(String sourceName) {
        return sessionMap.get(sourceName);
    }

    public boolean isSessionExist(String sourceName) {
        return sessionMap.containsKey(sourceName);
    }

    public Map<String, ClosableSession> getSessionMap() {
        return sessionMap;
    }

    public boolean isErrorExist() {
        if (!errorTrace.isEmpty()) {
            return true;
        }
        return false;
    }

}
