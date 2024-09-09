package mb.dnm.core.callback;

import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Map;

@Slf4j
public class TransactionCleanupCallback implements AfterProcessCallback {

    @Override
    public void afterProcess(ServiceContext ctx) {
        Map<String, TransactionContext> txCtxMap = ctx.getTransactionContextMap();

        boolean errorExist = ctx.isErrorExist();
        for (String executorName : txCtxMap.keySet()) {
            TransactionContext txCtx = txCtxMap.get(executorName);
            TransactionStatus txStatus = txCtx.getTransactionStatus();
            log.info("TxStatus: {}, Executor: {}", txStatus, executorName); //Logging for test
            if (txStatus != null) {
                if (!txStatus.isCompleted()) {
                    DataSourceTransactionManager txManager = DataSourceProvider.access().getTransactionManager(executorName);
                    if (errorExist) {
                        txManager.rollback(txStatus);
                        log.warn("[{}]Rollback transaction context for executor {}, Because an error trace is detected in the ServiceContext."
                                , ctx.getTxId(), executorName);
                    } else {
                        log.info("[{}]Commiting transaction context for executor {}", ctx.getTxId(), executorName);
                        txManager.commit(txStatus);
                    }
                }
            }
        }
    }

}
