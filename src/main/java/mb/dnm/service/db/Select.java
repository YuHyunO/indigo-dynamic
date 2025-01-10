package mb.dnm.service.db;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.db.*;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Database에 SELECT Query를 처리하여 결과를 가져온다.
 * Query에 대한 정보는 <code>InterfaceInfo</code> class의 <code>querySequence</code>로부터 가져온다.
 * <br>
 * SELECT parameter가 <code>List</code> 형태로 전달되는 경우 SELECT 문이 <code>List</code>의 size만큼 반복되어 합산된 결과가 Output 된다.
 * <br>
 * Input 데이터가 Null 인 경우에도 query는 수행된다.
 * handleResultSet 속성이 true 인 경우 ResultHandlingSupportFactory에 등록된 IterationGroup을 조작하여 Select 쿼리 결과를 한 건씩 핸들링 할 수 있다.
 *
 * @see mb.dnm.storage.InterfaceInfo#setQuerySequence(String)
 * @see mb.dnm.storage.InterfaceInfo#getQuerySequence()
 *
 * @author Yuhyun O
 * @version 2024.09.05
 * @Input Select query 시 사용될 parameter
 * @InputType <code>Map&lt;String, Object&gt;</code> or <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
 * @Output Select 쿼리의 결과 또는 Select 된 rows 의 개수(handleResultSet 속성이 true 인 경우)
 * @OutputType <code>List&lt;Map&lt;String, Object&gt;&gt;</code> or <code>int</code>(handleResultSet 속성이 true 인 경우)
 *
 * @Exceptions <code>IllegalArgumentException</code>: Input parameter의 type이 지원되지 않는 타입인 경우 <br> <code>InvalidServiceConfigurationException</code>: QuerySequnce queue에서 더 이상 실행할 query를 찾지 못했을 때
 * */

@Slf4j
@Setter
public class Select extends ParameterAssignableService implements Serializable {
    private static final long serialVersionUID = 2407107261293773545L;
    private boolean errorQueryMode = false;
    private boolean handleResultSet = false;
    private ResultHandlingSupportFactory resultHandlingSupportFactory;
    private String queryId;

    @Override
    public void process(ServiceContext ctx) {

        //(1) Get QueryMap and QueryExecutor.
        QueryMap queryMap = null;

        if (queryId != null) {
            queryMap = ctx.getQueryMap(queryId);

        } if (errorQueryMode) {
            if (!ctx.hasMoreErrorQueryMaps()) {
                throw new InvalidServiceConfigurationException(this.getClass(), "No more error query found in the query sequence queue");
            }
            queryMap = ctx.nextErrorQueryMap();
        } else {
            if (!ctx.hasMoreQueryMaps()) {
                throw new InvalidServiceConfigurationException(this.getClass(), "No more query found in the query sequence queue");
            }
            queryMap = ctx.nextQueryMap();
        }
        String executorName = queryMap.getExecutorName();
        Object constantExecutors = ctx.getContextParam("$constant_executor");
        if (constantExecutors instanceof Set) {
            if ( ((Set) constantExecutors).contains(executorName) ) {
                throw new InvalidServiceConfigurationException(this.getClass(), "The transaction named '" + executorName + "' is not mutable because it has been marked as constant. Use another query executor instead.");
            }
        }

        String txId = ctx.getTxId();
        TransactionContext txContext = ctx.getTransactionContext(queryMap);
        QueryExecutor executor = DataSourceProvider.access().getExecutor(executorName);

        //(2) Get parameter
        Object inValue = getInputValue(ctx);

        //(3) Prepare object for result
        List<Map<String, Object>> selectResult = new ArrayList<>();

        Map<String, Object> ctxInfoMap = ctx.getContextInformation();

        //대용량 결과 처리를 위한 객체(ResultHandlingSupport) 생성
        ResultHandlingSupport resultHandlingSupport = null;
        if (handleResultSet) {
            if (resultHandlingSupportFactory == null) {
                log.debug("[{}]Initializing resultHandlingSupportFactory object...", txId);
                resultHandlingSupportFactory = new ResultHandlingSupportFactory();
            }
            resultHandlingSupport = resultHandlingSupportFactory.getResultHandlingSupport(ctx);
            resultHandlingSupport.setExecutorName(queryMap.getExecutorName());
        }

        int selectedCnt = 0;
        //(4) Execute query when parameter is not null
        if (inValue != null) {
            List<Map<String, Object>> selectParameters = new ArrayList<>();
            try {
                if (inValue instanceof Map) {
                    Map<String, Object> param = (Map<String, Object>) inValue;
                    param.putAll(ctxInfoMap);
                    selectParameters.add(param);
                } else if (inValue instanceof List) {
                    selectParameters.addAll((List<Map<String, Object>>) inValue);
                }
            } catch (ClassCastException ce) {
                throw new IllegalArgumentException("The type of input parameter is invalid: " + inValue.getClass());
            }

            String queryId = queryMap.getQueryId();
            if (handleResultSet) {
                selectedCnt = executor.doHandleSelect(txContext, queryId, selectParameters, ctxInfoMap, resultHandlingSupport);
            } else {
                selectResult = executor.doSelects(txContext, queryId, selectParameters, ctxInfoMap);
            }
        } else { //(4) Execute query when parameter is null
            if (handleResultSet) {
                selectedCnt = executor.doHandleSelect(txContext, queryMap.getQueryId(), ctxInfoMap, resultHandlingSupport);
            } else {
                selectResult = executor.doSelect(txContext, queryMap.getQueryId(), ctxInfoMap);
            }
        }

        if (!handleResultSet) {
            selectedCnt = selectResult.size();
        }
        log.info("[{}]{} rows selected", ctx.getTxId(), selectedCnt);

        if (getOutput() != null) {
            if (!handleResultSet) {
                setOutputValue(ctx, selectResult);
            } else {
                setOutputValue(ctx, selectedCnt);
            }
        }
    }

    @Override
    public void setExceptionHandlingMode(boolean exceptionHandlingMode) {
        this.errorQueryMode = exceptionHandlingMode;
        this.exceptionHandlingMode = exceptionHandlingMode;
    }

    public void setQueryId(String queryId) {
        if (queryId == null)
            return;
        if (queryId.trim().isEmpty())
            return;
        this.queryId = queryId;
    }
}
