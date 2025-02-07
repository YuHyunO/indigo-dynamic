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
import java.util.Set;


/**
 * Database Delete 쿼리를 실행한다.<br>
 * 실행할 쿼리는 Mapper xml 파일에 등록이 되어있어야한다.
 * <br>
 * <br>
 * *<b>Input</b>: 쿼리 실행 시 사용할 파라미터<br>
 * *<b>Input type</b>: {@code Map<String, Object>}
 * <br>
 * <br>
 * *<b>Output</b>: Delete 쿼리 실행에 영향 받은 row 수<br>
 * *<b>Output type</b>: {@code int}
 * <br>
 * <br>
 *<pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;!-- {@link mb.dnm.storage.InterfaceInfo}의 querySequence에 등록된 queryId 순서대로 실행하는 경우--&gt;
 * &lt;bean class="mb.dnm.service.db.Delete"&gt;
 *     &lt;property name="input"                  value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                 value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;!-- {@link mb.dnm.storage.InterfaceInfo}의 querySequence에 등록된 특정 queryId를 지정하여 실행하는 경우--&gt;
 * &lt;bean class="mb.dnm.service.db.Delete"&gt;
 *     &lt;property name="queryId"                value="<span style="color: black; background-color: #FAF3D4;">queryId 만을 입력한다(executorName X / namespace X)</span>"/&gt;
 *     &lt;property name="input"                  value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                 value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 * &lt;/bean&gt;</pre>
 */
@Slf4j
@Setter
public class Delete extends ParameterAssignableService implements Serializable {

    private static final long serialVersionUID = -1231931765629075941L;
    private boolean errorQueryMode = false;
    private String queryId;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
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
        TransactionContext txContext = ctx.getTransactionContext(queryMap);
        QueryExecutor executor = DataSourceProvider.access().getExecutor(executorName);

        //(2) Get parameter
        Object inValue = getInputValue(ctx);

        //(3) Prepare object for result
        int updatedRows = 0;

        //(4) Execute query
        List<Map<String, Object>> updateParams = new ArrayList<>();
        Map<String, Object> ctxInfoMap = ctx.getContextInformation();
        if (inValue != null) {
            try {
                if (inValue instanceof Map) {
                    Map<String, Object> param = (Map<String, Object>) inValue;
                    updateParams.add(param);
                } else if (inValue instanceof List) {
                    updateParams.addAll((List<Map<String, Object>>) inValue);
                }
            } catch (ClassCastException ce) {
                throw new IllegalArgumentException("The type of input parameter is invalid: " + inValue.getClass());
            }
        }

        updatedRows = executor.doBatchUpdate(txContext, queryMap.getQueryId(), updateParams, ctxInfoMap);

        log.info("[{}]{} rows deleted", ctx.getTxId(), updatedRows);

        if (getOutput() != null) {
            setOutputValue(ctx, updatedRows);
        }
    }

    @Override
    public void setExceptionHandlingMode(boolean exceptionHandlingMode) {
        this.errorQueryMode = exceptionHandlingMode;
        this.exceptionHandlingMode = exceptionHandlingMode;
    }

    /**
     * {@code Delete} service 가 사용할 queryId를 등록한다.<br>
     * queryId 속성을 사용하려면 사용하려는 쿼리가 {@link mb.dnm.storage.InterfaceInfo}의 querySequence에 등록되어 있어야 한다.
     * <br>
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.storage.InterfaceInfo""&gt;
     *     &lt;property name="interfaceId"            value="IF_TEST"/&gt;
     *                  .
     *                  .
     *                  .
     *     &lt;property name="sourceCode"             value="SRC"/&gt;
     *     &lt;property name="targetCode"             value="TGT"/&gt;
     *     &lt;property name="querySequence"          value="SRC_DB@{if_id}.SELECT, TGT_DB@{if_id}.INSERT, SRC_DB@{if_id}.UPDATE"/&gt;
     *     &lt;property name="serviceId"              value="SELECT_INSERT_UPDATE"/&gt;
     * &lt;/bean&gt;</pre>
     * <br>
     * Example : setQueryId
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *     &lt;property name="queryId"             value="SELECT"/&gt;
     *     or
     *     &lt;property name="queryId"             value="INSERT"/&gt;
     *     or
     *     &lt;property name="queryId"             value="UPDATE"/&gt;</pre>
     * <b>주의사항</b>: queryId 속성을 사용하는 경우, 해당 service-strategy 에서는 다른 모든 DB 쿼리작업을 하는 Service에서도 queryId 속성을 사용하여야 한다.
     * @param queryId the query id
     */
    public void setQueryId(String queryId) {
        if (queryId == null)
            return;
        if (queryId.trim().isEmpty())
            return;
        this.queryId = queryId;
    }
}
