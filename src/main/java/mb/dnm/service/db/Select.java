package mb.dnm.service.db;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.access.db.QueryExecutor;
import mb.dnm.access.db.QueryMap;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Database에 SELECT Query를 처리하여 결과를 가져온다.
 * Query에 대한 정보는 <code>InterfaceInfo</code> class의 <code>querySequence</code>로부터 가져온다.
 * <br>
 * SELECT parameter가 <code>List</code> 형태로 전달되는 경우 SELECT 문이 <code>List</code>의 size만큼 반복되어 합산된 결과가 Output 된다.
 * <br>
 * Input 데이터가 Null 인 경우에도 query는 수행된다.
 *
 * @see mb.dnm.storage.InterfaceInfo#setQuerySequence(String)
 * @see mb.dnm.storage.InterfaceInfo#getQuerySequence()
 *
 * @author Yuhyun O
 * @version 2024.09.05
 * @Input Select query 시 사용될 parameter
 * @InputType <code>Map&lt;String, Object&gt;</code> or <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
 * @Output Select 쿼리의 결과
 * @OutputType <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
 *
 * @Exceptions <code>IllegalArgumentException</code>: Input parameter의 type이 지원되지 않는 타입인 경우 <br> <code>InvalidServiceConfigurationException</code>: QuerySequnce queue에서 더 이상 실행할 query를 찾지 못했을 때
 * */

@Slf4j
public class Select extends ParameterAssignableService {

    @Override
    public void process(ServiceContext ctx) {

        if (!ctx.hasMoreQueryMaps()) {
            throw new InvalidServiceConfigurationException(this.getClass(), "No more query found in the query sequence queue");
        }

        //(1) Get QueryMap and QueryExecutor.
        QueryMap queryMap = ctx.nextQueryMap();
        TransactionContext txContext = ctx.getTransactionContext(queryMap);
        QueryExecutor executor = DataSourceProvider.access().getExecutor(queryMap.getExecutorName());

        //(2) Get parameter
        Object inValue = getInputValue(ctx);

        //(3) Prepare object for result
        List<Map<String, Object>> selectResult = new ArrayList<>();

        //(4) Execute query when parameter is not null
        if (inValue != null) {
            List<Map<String, Object>> selectParameters = new ArrayList<>();
            try {
                if (inValue instanceof Map) {
                    Map<String, Object> param = (Map<String, Object>) inValue;
                    selectParameters.add(param);
                } else if (inValue instanceof List) {
                    selectParameters.addAll((List<Map<String, Object>>) inValue);
                }
            } catch (ClassCastException ce) {
                throw new IllegalArgumentException("The type of input parameter is invalid: " + inValue.getClass());
            }

            String queryId = queryMap.getQueryId();
            selectResult = executor.doSelects(txContext, queryId, selectParameters);
        } else { //(4) Execute query when parameter is null
            selectResult = executor.doSelect(txContext, queryMap.getQueryId(), null);
        }

        log.info("[{}]{} rows selected", ctx.getTxId(), selectResult.size());

        if (getOutput() != null) {
            setOutputValue(ctx, selectResult);
        }
    }
}
