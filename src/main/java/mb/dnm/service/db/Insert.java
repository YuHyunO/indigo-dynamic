package mb.dnm.service.db;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.access.db.QueryExecutor;
import mb.dnm.access.db.QueryMap;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Input으로 받은 데이터를 파라미터로 하여 INSERT query를 처리한다.
 * <br>
 * Input 데이터가 Null 인 경우에도 query는 수행된다.
 *
 * @see mb.dnm.storage.InterfaceInfo#setQuerySequence(String)
 * @see mb.dnm.storage.InterfaceInfo#getQuerySequence()
 *
 * @author Yuhyun O
 * @version 2024.09.05
 * @Input Insert query 시 사용될 parameter
 * @InputType <code>Map&lt;String, Object&gt;</code> or <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
 * @Output Insert 된 rows 의 수
 * @OutputType <code>int</code>
 *
 * @Exceptions <code>IllegalArgumentException</code>: Input parameter의 type이 지원되지 않는 타입인 경우 <br> <code>InvalidServiceConfigurationException</code>: QuerySequnce queue에서 더 이상 실행할 query를 찾지 못했을 때
 * */

@Slf4j
@Setter
public class Insert extends ParameterAssignableService implements Serializable {
    private static final long serialVersionUID = 5157825800696290202L;
    private boolean errorQueryMode = false;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        //(1) Get QueryMap and QueryExecutor.
        QueryMap queryMap = null;

        if (errorQueryMode) {
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
        if (executorName.equals(ctx.getContextParam("$constant_executor"))) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The transaction named '" + executorName + "' is not mutable because it has been marked as constant. Use another query executor instead.");
        }
        TransactionContext txContext = ctx.getTransactionContext(queryMap);
        QueryExecutor executor = DataSourceProvider.access().getExecutor(executorName);

        //(2) Get parameter
        Object inValue = getInputValue(ctx);

        //(3) Prepare object for result
        int insertedRows = 0;

        //(4) Execute query
        List<Map<String, Object>> insertParams = new ArrayList<>();
        Map<String, Object> ctxInfoMap = ctx.getContextInformation();
        if (inValue != null) {
            try {
                if (inValue instanceof Map) {
                    Map<String, Object> param = (Map<String, Object>) inValue;
                    insertParams.add(param);
                } else if (inValue instanceof List) {
                    insertParams.addAll((List<Map<String, Object>>) inValue);
                }
            } catch (ClassCastException ce) {
                throw new IllegalArgumentException("The type of input parameter is invalid: " + inValue.getClass());
            }
        }

        insertedRows = executor.doBatchInsert(txContext, queryMap.getQueryId(), insertParams, ctxInfoMap);

        log.info("[{}]{} rows inserted", ctx.getTxId(), insertedRows);

        if (getOutput() != null) {
            setOutputValue(ctx, insertedRows);
        }
    }

    @Override
    public void setExceptionHandlingMode(boolean exceptionHandlingMode) {
        this.errorQueryMode = exceptionHandlingMode;
        this.exceptionHandlingMode = exceptionHandlingMode;
    }

}
