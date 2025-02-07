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

/**
 * {@code ServiceContext}는 인터페이스 이벤트를 분배하는 {@code Dispatcher}의 역할을 하는 객체에서 생성되어 {@link mb.dnm.core.ServiceProcessor} 에서 수행되는 모든 프로세스를 관통하는 객체이다.<br>
 * Service-Chaining은 여러 개의 Service를 특정 순서에 따라 조합하여 실행하는 과정이다. 각각의 Service는 특정 기능(예: 데이터베이스 작업, 파일 처리 등)을 수행하는 최소 단위의 컴포넌트로, Service 자신이 수행하는 작업에만 집중한다.<br>
 * 따라서 개별 Service는 어떤 데이터가 처리되어야 하는지 또는 프로세스의 전체 맥락에 대해 알지 못하기 때문에 각 Service에서 사용될 데이터를 전달하고 일관성을 유지하기 위해 Service Context 객체가 사용된다.<br>
 * ServiceContext는 Service-Chaining 과정에서 메타데이터 및 Input 과 Output 데이터를 관리하고, 데이터 흐름과 상태를 유지하는 중요한 객체이다.
 */
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

    /**
     * 새로운 {@code ServiceContext} 객체를 생성한다.
     *
     * @param info {@link InterfaceInfo} 객체
     */
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

    /**
     * 실행된 {@link Service} 클래스를 실행 Trace 에 추가한다.
     *
     * @param service 실행된 {@link Service} 클래스
     */
    public void addServiceTrace(Class<? extends Service> service) {
        serviceTrace.add(service);
    }

    /**
     * 실행된 {@link Service} 클래스 List를 실행 Trace 에 추가한다.
     *
     * @param serviceTrace the service trace
     */
    public void addServiceTraces(List<Class<? extends Service>> serviceTrace) {
        serviceTrace.addAll(serviceTrace);
    }

    /**
     * 메소드가 실행된 시점까지의 {@code ServiceTrace} 를 JSON 형태로 가져온다.
     *
     * @return the service trace message
     */
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


    /**
     * 메소드가 실행된 시점까지의 {@code ServiceTrace} 를 {@code Map} 형태로 가져온다.
     *
     * @return the service trace map
     */
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


    /**
     * Add inner service trace.
     *
     * @param externalServiceIdx the external service idx
     * @param innerServiceIdx    the inner service idx
     * @param service            the service
     */
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

    /**
     * Add error trace.
     *
     * @param service   the service
     * @param throwable the throwable
     */
    public void addErrorTrace(Class<? extends Service> service, Throwable throwable) {
        errorTrace.put(service, new ErrorTrace(throwable));
    }


    /**
     * ErrorTrace 메시지를 JSON 형식으로 가져온다.
     *
     * @return the error trace message
     */
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

    /**
     * Gets error trace map.
     *
     * @return the error trace map
     */
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

    /**
     * {@link mb.dnm.core.ServiceProcessor}에서 에러 발생시 생성되는 {@link ErrorTrace} 객체를 {@code ServiceContext} 에 저장한다.
     *
     * @return the error traces
     */
    public List<ErrorTrace> getErrorTraces() {
        List<ErrorTrace> errorTraceList = new ArrayList<>();
        for (Map.Entry<Class<? extends Service>, ErrorTrace> entry : errorTrace.entrySet()) {
            errorTraceList.add(entry.getValue());
        }
        return errorTraceList;
    }

    /**
     * Gets error trace.
     *
     * @param service the service
     * @return the error trace
     */
    @Deprecated
    public ErrorTrace getErrorTrace(Class<? extends Service> service) {
        return errorTrace.get(service);
    }

    /**
     * {@code ServiceContext}의 인터페이스 ID 를 가져온다.
     *
     * @return the interface id
     */
    public String getInterfaceId() {
        return info.getInterfaceId();
    }

    /**
     * 매번의 {@code Service} 종료시점에 종료시간을 기록한다.
     */
    public void stampEndTime() {
        endTime = new Date();
    }

    /**
     * {@code ServiceContext}에 {@code key:value} 형태로 데이터를 저장한다.<br>
     * {@link Service}에서 저장되는 모든 {@code Input/Output} 데이터는 이 메소드를 통해 저장된다.<br>
     * 저장하려는 데이터와 동일한 {@code key}를 가진 데이터가 이미 {@code ServiceContext}에 존재하는 경우 덮어쓰기 처리된다.
     *
     * @param key   the key
     * @param value the value
     */
    public void addContextParam(String key, Object value) {
        contextParams.put(key, value);
    }

    /**
     * {@code ServiceContext}에 {@code Map<String, Object>} 형태로 데이터를 저장한다.<br>
     * 저장하려는 데이터와 동일한 {@code key}를 가진 데이터가 이미 {@code ServiceContext}에 존재하는 경우 덮어쓰기 처리된다.
     *
     * @param params the params
     */
    public void addContextParams(Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            this.contextParams.putAll(params);
        }
    }

    /**
     * {@code key}를 사용하여 {@code ServiceContext}에서 저장되어있는 데이터를 제거한다.
     *
     * @param key the key
     */
    public void deleteContextParam(String key) {
        if (contextParams.containsKey(key)) {
            contextParams.remove(key);
        }
    }

    /**
     * {@code key}를 사용하여 {@code ServiceContext}에서 저장되어있는 데이터를 가져온다.
     *
     * @param key the key
     * @return the context param
     * @see ServiceContext#addContextParam(String, Object) ServiceContext#addContextParam(String, Object)ServiceContext#addContextParam(String, Object)
     * @see ServiceContext#addContextParams(Map) ServiceContext#addContextParams(Map)ServiceContext#addContextParams(Map)
     */
    public Object getContextParam(String key) {
        if (key == null)
            return null;
        return contextParams.get(key);
    }

    /**
     * {@code ServiceContext}에 저장되어있는 모든 데이터를 가져온다.
     *
     * @return the context params
     * @see ServiceContext#addContextParam(String, Object) ServiceContext#addContextParam(String, Object)ServiceContext#addContextParam(String, Object)
     * @see ServiceContext#addContextParams(Map) ServiceContext#addContextParams(Map)ServiceContext#addContextParams(Map)
     */
    public Map<String, Object> getContextParams() {
        return contextParams;
    }

    /**
     * {@link InterfaceInfo} 의 {@code dynamicCodeSequence}에 설정된 DynamicCode의 ID를 순차적으로 가져온다.
     *
     * @return the next dynamicCodeId
     */
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

    /**
     * {@link InterfaceInfo} 의 {@code dynamicCodeSequence}에 실행할 {@code dynamicCode} 가 남아있는지 확인한다.
     *
     * @return 잔여 DynamicCode 여부
     */
    public boolean hasMoreDynamicCodes() {
        String[] codeSequence = info.getDynamicCodeSequence();
        int seqSize = codeSequence.length;
        if (currentDynamicCodeOrder < seqSize)
            return true;
        return false;
    }

    /**
     * {@link InterfaceInfo} 의 {@code errorDynamicCodeSequence}에 설정된 ErrorDynamicCode의 ID를 순차적으로 가져온다.<br>
     * {@link ServiceContext#nextErrorDynamicCodeId()} 메소드는 {@link mb.dnm.core.ServiceProcessor}의 Error-Handling 과정에서만 사용된다.
     *
     * @return the string
     */
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

    /**
     * {@link InterfaceInfo} 의 {@code errorDynamicCodeSequence}에 실행할 {@code errorDynamicCodeSequence} 가 남아있는지 확인한다.
     *
     * @return the boolean
     */
    public boolean hasMoreErrorDynamicCodes() {
        String[] codeSequence = info.getErrorDynamicCodeSequence();
        int seqSize = codeSequence.length;
        if (currentErrorDynamicCodeOrder < seqSize)
            return true;
        return false;
    }

    /**
     * {@link InterfaceInfo} 의 {@code querySequence}에 실행할 {@code queryId} 가 남아있는지 확인한다.
     *
     * @return the boolean
     */
    public boolean hasMoreQueryMaps() {
        String[] querySequence = info.getQuerySequence();
        int seqSize = querySequence.length;
        if (currentQueryOrder < seqSize)
            return true;
        return false;
    }


    /**
     * {@code InterfaceInfo}의 querySequence 에 등록된 queryId 중 파라미터로 전달받은 id 와 일치하는 id를 찾아  {@link QueryMap}을 반환한다.
     *
     * @param id the queryId
     * @return the query map
     */
    public QueryMap getQueryMap(String id) {
        String[] querySequence = info.getQuerySequence();
        if (querySequence == null) {
            throw new NoSuchElementException("Query sequence is null");
        }

        if (id == null) {
            throw new NullPointerException("The parameter 'queryId' must not be null.");
        }

        String query = null;
        String checkId = "." + id.replace(" ", "");
        for (String q : querySequence) {
            if (q.endsWith(checkId)) {
                query = q;
                break;
            }
        }
        if (query == null) {
            throw new NoSuchElementException("There is no query with id " + id);
        }

        int executorNameIdx = query.indexOf('$');
        String executorName = query.substring(0, executorNameIdx);
        String queryId = query.substring(executorNameIdx + 1);

        QueryMap qmap = new QueryMap(executorName, queryId);
        qmap.setTimeoutSecond(info.getTxTimeoutSecond());
        addTransactionContext(qmap);

        return qmap;
    }

    /**
     * {@link InterfaceInfo} 에 등록된 querySequence 에서 다음 실행될 쿼리의 {@link QueryMap} 을 순차적으로 가져온다.<br>
     * {@link InterfaceInfo}의 querySequence에서 다음 실행될 쿼리의 인덱스는 {@link ServiceContext#getCurrentQueryOrder()} 메소드의 실행결과와 동일하다.
     *
     * @return the query map
     */
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

        return qmap;
        //return new QueryMap(executorName, queryId);
    }

    /**
     * {@link InterfaceInfo} 의 {@code errorQuerySequence}에 실행할 {@code queryId} 가 남아있는지 확인한다.
     *
     * @return the boolean
     */
    public boolean hasMoreErrorQueryMaps() {
        String[] querySequence = info.getErrorQuerySequence();
        int seqSize = querySequence.length;
        if (currentErrorQueryOrder < seqSize)
            return true;
        return false;
    }

    /**
     * {@link InterfaceInfo} 에 등록된 errorQuerySequence 에서 다음 실행될 에러 쿼리의 {@link QueryMap} 을 순차적으로 가져온다.
     * {@link InterfaceInfo}의 errorQuerySequence에서 다음 실행될 쿼리의 인덱스는 {@link ServiceContext#getCurrentErrorQueryOrder()} 메소드의 실행결과와 동일하다.
     * @return the query map
     */
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

        return qmap;
        //return new QueryMap(executorName, queryId);
    }

    /**
     * Add transaction context.
     *
     * @param queryMap the query map
     */
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
     * Sets group transaction.
     *
     * @param executorName   the executor name
     * @param groupTxEnabled the group tx enabled
     * @return 'true' if the TransactionContext is newly created. 'false' if the TransactionContext is already exist. <br> But the group transaction property is always enabled in both cases.
     */
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

    /**
     * Gets transaction context.
     *
     * @param queryMap the query map
     * @return the transaction context
     */
    public TransactionContext getTransactionContext(QueryMap queryMap) {
        return txContextMap.get(queryMap.getExecutorName());
    }

    /**
     * Gets transaction context.
     *
     * @param executorName the executor name
     * @return the transaction context
     */
    public TransactionContext getTransactionContext(String executorName) {
        return txContextMap.get(executorName);
    }

    /**
     * Gets transaction context map.
     *
     * @return the transaction context map
     */
    public Map<String, TransactionContext> getTransactionContextMap() {
        return txContextMap;
    }

    /**
     * Add session.
     *
     * @param sourceName the source name
     * @param session    the session
     */
    public void addSession(String sourceName, ClosableStreamWrapper session) {
        sessionMap.put(sourceName, session);
    }

    /**
     * Gets session.
     *
     * @param sourceName the source name
     * @return the session
     */
    public ClosableStreamWrapper getSession(String sourceName) {
        return sessionMap.get(sourceName);
    }

    /**
     * Is session exist boolean.
     *
     * @param sourceName the source name
     * @return the boolean
     */
    public boolean isSessionExist(String sourceName) {
        return sessionMap.containsKey(sourceName);
    }

    /**
     * Gets session map.
     *
     * @return the session map
     */
    public Map<String, ClosableStreamWrapper> getSessionMap() {
        return sessionMap;
    }

    /**
     * Is error exist boolean.
     *
     * @return the boolean
     */
    public boolean isErrorExist() {
        if (!errorTrace.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * {@code ServiceContext} 에 message 를 입력한다.
     *
     * @param msg the msg
     */
    public void setMsg(StringBuilder msg) {
        this.msg = msg;
    }

    /**
     * {@code ServiceContext} 에 message 를 입력한다.
     *
     * @param msg the msg
     */
    public void setMsg(String msg) {
        this.msg = new StringBuilder(msg);
    }


    /**
     * {@code ServiceContext} 에 입력된 message 를 가져온다.
     *
     * @return the msg
     */
    public String getMsg() {
        if (msg == null) {
            return null;
        }
        return msg.toString();
    }

    /**
     * {@code ServiceContext} 의 메타데이터를 가져온다.
     * <br><br>
     * </pre>
     * <table border="1">
     *      <thead>
     *          <tr>
     *              <th>Key</th>
     *              <th>Description</th>
     *          </tr>
     *      </thead>
     *      <tbody>
     *          <tr>
     *              <td>$tx_id</td>
     *              <td>트랜잭션 아이디</td>
     *          </tr>
     *          <tr>
     *              <td>$if_id</td>
     *              <td>인터페이스 아이디</td>
     *          </tr>
     *          <tr>
     *              <td>$tx_msg</td>
     *              <td>메시지</td>
     *          </tr>
     *          <tr>
     *              <td>$process_status</td>
     *              <td>프로세스 상태코드[P:진행중, S:정상, F:오류]</td>
     *          </tr>
     *          <tr>
     *              <td>$start_time_timestamp</td>
     *              <td>Service-Chaining 시작 시간 [yyyyMMddHHmmssSSS]</td>
     *          </tr>
     *          <tr>
     *              <td>$start_time_date</td>
     *              <td>Service-Chaining 시작 시간 [yyyyMMddHHmmss]</td>
     *          </tr>
     *          <tr>
     *              <td>$end_time_timestamp</td>
     *              <td>Service 완료 시간 [yyyyMMddHHmmssSSS]</td>
     *          </tr>
     *          <tr>
     *              <td>$end_time_date</td>
     *              <td>Service 완료 시간 [yyyyMMddHHmmss]</td>
     *          </tr>
     *          <tr>
     *              <td>$YYYY</td>
     *              <td>현재 시간 [yyyy]</td>
     *          </tr>
     *          <tr>
     *              <td>$YYYYMM</td>
     *              <td>현재 시간 [yyyyMM]</td>
     *          </tr>
     *          <tr>
     *              <td>$YYYYMMDD</td>
     *              <td>현재 시간 [yyyyMMdd]</td>
     *          </tr>
     *          <tr>
     *              <td>$YYYYMMDDHHmmss</td>
     *              <td>현재 시간 [yyyyMMddHHmmss]</td>
     *          </tr>
     *          <tr>
     *              <td>$HHmmss</td>
     *              <td>현재 시간 [$HHmmss]</td>
     *          </tr>
     *          <tr>
     *              <td>$HHmm</td>
     *              <td>현재 시간 [HHmm]</td>
     *          </tr>
     *          <tr>
     *              <td>$iter_position</td>
     *              <td>{@link mb.dnm.service.general.IterationGroup}이 사용되는 경우 반복횟수.<br>
     *              {@code ServiceContext} 가 매번의 반복에서 재생성되는 경우 매번 초기화 된다.<br>
     *              따라서 {@link mb.dnm.service.general.IterationGroup} 내에서 사용될 때만 유효하다.
     *              </td>
     *          </tr>
     *          <tr>
     *              <td>$total_iter_position</td>
     *              <td>{@link mb.dnm.service.general.IterationGroup} 에서 총 반복횟수.<br>
     *              {@code ServiceContext} 가 매번의 반복에서 재생성되는 경우와 관계없이 합산된다.
     *              </td>
     *          </tr>
     *      </tbody>
     *  </table>
     *
     * @return the context information
     */
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
        infoMap.put("$HHmmss", TimeUtil.curDate(TimeUtil.HHmmss));
        infoMap.put("$HHmm", TimeUtil.curDate(TimeUtil.HHmm));
        infoMap.put("$iter_position", contextParams.get("$iter_position"));
        infoMap.put("$total_iter_position", contextParams.get("$total_iter_position"));
        return infoMap;
    }

    /**
     * The type Inner service trace.
     */
    public class InnerServiceTrace implements Serializable {
        private static final long serialVersionUID = -7111196943824771103L;
        @Getter
        private Map<Integer, InnerServiceTracePair> traceMap;

        /**
         * Instantiates a new Inner service trace.
         */
        public InnerServiceTrace() {
            traceMap = new LinkedHashMap<>();
        }

        /**
         * Add trace.
         *
         * @param innerServiceIdx the inner service idx
         * @param serviceClass    the service class
         */
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

        /**
         * Gets inner service trace.
         *
         * @param innerServiceIdx the inner service idx
         * @return the inner service trace
         */
        public InnerServiceTracePair getInnerServiceTrace(int innerServiceIdx) {
            return traceMap.get(innerServiceIdx);
        }

    }

    /**
     * The type Inner service trace pair.
     */
    @Getter
    public class InnerServiceTracePair implements Serializable {
        private static final long serialVersionUID = -8073101364903503521L;
        private final Class<? extends Service> innerServiceClass;
        private int iterationCount = 0;

        /**
         * Instantiates a new Inner service trace pair.
         *
         * @param innerServiceClass the inner service class
         */
        public InnerServiceTracePair(Class<? extends Service> innerServiceClass) {
            this.innerServiceClass = innerServiceClass;
        }

        /**
         * Stamp trace.
         *
         * @param innerServiceClass the inner service class
         */
        public void stampTrace(Class<? extends Service> innerServiceClass) {
            if (this.innerServiceClass == innerServiceClass) {
                ++iterationCount;
            }
        }

    }

    /**
     * {@link InterfaceInfo}의 query 시퀀스 큐의 query 순서를 수동으로 설정한다.<br>
     * {@link InterfaceInfo}의 querySequence의 인덱스는 0부터 시작한다.<br>
     *
     * @param currentQueryOrder the current query order
     * @see InterfaceInfo
     */
    public void setCurrentQueryOrder(int currentQueryOrder) {
        this.currentQueryOrder = currentQueryOrder;
    }

    /**
     * {@link InterfaceInfo}의 error query 시퀀스 큐의 query 순서를 수동으로 설정한다.<br>
     * {@link InterfaceInfo}의 errorQuerySequence의 인덱스는 0부터 시작한다.<br>
     *
     * @param currentErrorQueryOrder the current error query order
     */
    public void setCurrentErrorQueryOrder(int currentErrorQueryOrder) {
        this.currentErrorQueryOrder = currentErrorQueryOrder;
    }

    /**
     * {@link InterfaceInfo}의 DynamicCode 시퀀스 큐의 DynamicCode 순서를 수동으로 설정한다.<br>
     * {@link InterfaceInfo}의 dynamicCodeSequence의 인덱스는 0부터 시작한다.<br>
     *
     * @param currentDynamicCodeOrder the current dynamic code order
     */
    public void setCurrentDynamicCodeOrder(int currentDynamicCodeOrder) {
        this.currentDynamicCodeOrder = currentDynamicCodeOrder;
    }

    /**
     * {@link InterfaceInfo}의 error DynamicCode 시퀀스 큐의 DynamicCode 순서를 수동으로 설정한다.<br>
     * {@link InterfaceInfo}의 errorDynamicCodeSequence의 인덱스는 0부터 시작한다.<br>
     *
     * @param currentErrorDynamicCodeOrder the current error dynamic code order
     */
    public void setCurrentErrorDynamicCodeOrder(int currentErrorDynamicCodeOrder) {
        this.currentErrorDynamicCodeOrder = currentErrorDynamicCodeOrder;
    }

    /**
     * 프로세스 상태코드를 설정한다.<br><br>
     * {@code ServiceContext}최초 생성 시 : {@link ProcessCode#IN_PROCESS}<br>
     * {@link mb.dnm.core.ServiceProcessor} 에서 매번의 {@link Service} 가 성공적으로 종료될 때 : {@link ProcessCode#SUCCESS}<br>
     * 에러 발생 시 : {@link ProcessCode#SERVICE_FAILURE}
     *
     * @param processStatus the process status
     * @see ProcessCode
     */
    public void setProcessStatus(ProcessCode processStatus) {
        this.processStatus = processStatus;
    }

    /**
     * 프로세스의 진행여부를 결정한다.
     * {@code false}로 설정하는 경우 프로세스가 중단된다.
     * @param processOn the process on
     */
    public void setProcessOn(boolean processOn) {
        this.processOn = processOn;
    }

    /**
     * 이 메소드를 사용하는 시점의 프로세스의 상태 코드를 가져온다.
     *
     * @return the process status
     */
    public ProcessCode getProcessStatus() {
        return processStatus;
    }

    /**
     * Gets current error dynamic code order.
     *
     * @return the current error dynamic code order
     */
    public int getCurrentErrorDynamicCodeOrder() {
        return currentErrorDynamicCodeOrder;
    }

    /**
     * Gets current dynamic code order.
     *
     * @return the current dynamic code order
     */
    public int getCurrentDynamicCodeOrder() {
        return currentDynamicCodeOrder;
    }

    /**
     * Gets current error query order.
     *
     * @return the current error query order
     */
    public int getCurrentErrorQueryOrder() {
        return currentErrorQueryOrder;
    }

    /**
     * Gets current query order.
     *
     * @return the current query order
     */
    public int getCurrentQueryOrder() {
        return currentQueryOrder;
    }

    /**
     * 프로세스의 중단/진행 여부를 확인한다.
     *
     * @return the boolean
     */
    public boolean isProcessOn() {
        return processOn;
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

    /**
     * To map map.
     *
     * @return the map
     */
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
