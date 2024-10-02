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


/**
 * Database의 Cursor 를 open 한다.
 * <code>Fetch</code> 서비스와 연계해서 사용한다.<br>
 *
 * <i>이 서비스는 트랜잭션 그룹이 열려있는 경우에만 사용 가능하다.(StartTransaction 서비스 참고)</i>
 *
 * @see mb.dnm.service.db.Fetch
 * @see mb.dnm.service.db.StartTransaction
 * @see mb.dnm.service.db.EndTransaction
 * @see mb.dnm.storage.InterfaceInfo#setQuerySequence(String)
 * @see mb.dnm.storage.InterfaceInfo#getQuerySequence()
 *
 * @author Yuhyun O
 * @version 2024.09.23
 *
 * @Throws cursor문이 잘못되었거나 Cursor를 open할 수 없는 경우
 * */

@Slf4j
@Setter
public class OpenCursor extends ParameterAssignableService {
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

        //(2) Prepare object for result and do fetch
        executor.doOpenCursor(txContext, queryMap.getQueryId());
        log.info("[{}]Cursor opened", ctx.getTxId());

    }

    @Override
    public void setExceptionHandlingMode(boolean exceptionHandlingMode) {
        this.errorQueryMode = exceptionHandlingMode;
        this.exceptionHandlingMode = exceptionHandlingMode;
    }
}
