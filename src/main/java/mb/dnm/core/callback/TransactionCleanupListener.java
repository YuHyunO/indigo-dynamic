package mb.dnm.core.callback;

import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import mb.dnm.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Map;

@Slf4j
public class TransactionCleanupListener implements AfterProcessListener{

    @Override
    public void afterProcess(ServiceContext ctx) {
        Map<String, TransactionContext> txCtxMap = ctx.getTransactionContextMap();

        for (String executorName : txCtxMap.keySet()) {
            TransactionContext txCtx = txCtxMap.get(executorName);
            TransactionStatus txStatus = txCtx.getTransactionStatus();
            if (txStatus != null) {
                if (!txStatus.isCompleted()) {
                    DataSourceTransactionManager txManager = DataSourceProvider.access().getTransactionManager(executorName);
                    Throwable error = txCtx.getError();
                    if (error != null) {
                        txManager.rollback(txStatus);
                        log.warn("[{}]Rollback transaction context for executor {}, Because an error is exist in the TransactionContext.\n{}"
                                , ctx.getTxId(), executorName, MessageUtil.toString(error));
                    } else {
                        log.debug("[{}]Cleaning up transaction context for executor {}", ctx.getTxId(), executorName);
                        txManager.commit(txStatus);
                    }
                }
            }
        }
    }

}
