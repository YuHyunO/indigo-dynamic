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
import java.util.Set;


/**
 * The type Open cursor.
 */
@Slf4j
@Setter
public class OpenCursor extends ParameterAssignableService implements Serializable {
    private static final long serialVersionUID = -6182495086850851902L;
    private boolean errorQueryMode = false;
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
        TransactionContext txContext = ctx.getTransactionContext(queryMap);
        QueryExecutor executor = DataSourceProvider.access().getExecutor(executorName);

        //(2) Prepare object for result and do fetch
        executor.doOpenCursor(txContext, queryMap.getQueryId());
        log.info("[{}]Cursor opened", ctx.getTxId());

    }

    @Override
    public void setExceptionHandlingMode(boolean exceptionHandlingMode) {
        this.errorQueryMode = exceptionHandlingMode;
        this.exceptionHandlingMode = exceptionHandlingMode;
    }

    /**
     * Sets query id.
     *
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
