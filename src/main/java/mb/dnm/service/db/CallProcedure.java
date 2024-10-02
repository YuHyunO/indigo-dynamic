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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Database의 Stored procedure 또는 Function 을 호출한다.
 *
 * @see mb.dnm.storage.InterfaceInfo#setQuerySequence(String)
 * @see mb.dnm.storage.InterfaceInfo#getQuerySequence()
 *
 * @author Yuhyun O
 * @version 2024.09.22
 * @Input Stored procedure 또는 Function 호출 시 사용될 parameter
 * @InputType <code>Map&lt;String, Object&gt;</code> or <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
 * @Output Stored procedure 또는 Function 의 호출 결과
 * @OutputType <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
 *
 * @Throws <code>IllegalArgumentException</code>: Input parameter의 type이 지원되지 않는 타입인 경우 <br> <code>InvalidServiceConfigurationException</code>: QuerySequnce queue에서 더 이상 실행할 query를 찾지 못했을 때
 * */

@Slf4j
@Setter
public class CallProcedure extends ParameterAssignableService {
    private boolean errorQueryMode = false;

    @Override
    public void process(ServiceContext ctx) {
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

        TransactionContext txContext = ctx.getTransactionContext(queryMap);
        QueryExecutor executor = DataSourceProvider.access().getExecutor(queryMap.getExecutorName());

        //(2) Get parameter
        Object inValue = getInputValue(ctx);

        //(3) Prepare object for result
        List<Map<String, Object>> callResult = new ArrayList<>();
        Map<String, Object> ctxInfoMap = ctx.getContextInformation();

        //(4) Execute query when parameter is not null
        if (inValue != null) {
            List<Map<String, Object>> callParameters = new ArrayList<>();
            try {
                if (inValue instanceof Map) {
                    Map<String, Object> param = (Map<String, Object>) inValue;
                    callParameters.add(param);
                } else if (inValue instanceof List) {
                    callParameters.addAll((List<Map<String, Object>>) inValue);
                }
            } catch (ClassCastException ce) {
                throw new IllegalArgumentException("The type of input parameter is invalid: " + inValue.getClass());
            }

            callResult = executor.doCall(txContext, queryMap.getQueryId(), callParameters, ctxInfoMap);
        } else { //(4) Execute query when parameter is null
            callResult = executor.doCall(txContext, queryMap.getQueryId(), null, ctxInfoMap);
        }

        log.info("[{}]{} rows selected", ctx.getTxId(), callResult.size());

        if (getOutput() != null) {
            setOutputValue(ctx, callResult);
        }
    }
}
