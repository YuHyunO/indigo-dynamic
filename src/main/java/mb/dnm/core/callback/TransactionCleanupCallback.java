package mb.dnm.core.callback;

import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Map;

/**
 * 하나의 서비스 프로세스가 종료될 때 마다 commit 또는 rollback 되지 않은 DB 트랜잭션을 처리하는 Callback 클래스이다.<br>
 * <code>ServiceProcessor</code>에 기본 callback으로 등록되어 있다.
 *
 * @see mb.dnm.core.callback.AfterProcessCallback
 *
 * @author Yuhyun O
 * @version 2024.09.05
 * */
@Slf4j
public class TransactionCleanupCallback implements AfterProcessCallback {

    @Override
    public void afterProcess(ServiceContext ctx) {
        Map<String, TransactionContext> txCtxMap = ctx.getTransactionContextMap();

        boolean errorExist = ctx.isErrorExist();
        for (Map.Entry<String, TransactionContext> entry : txCtxMap.entrySet()) {
            String executorName = entry.getKey();
            TransactionContext txCtx = entry.getValue();
            TransactionStatus txStatus = txCtx.getTransactionStatus();
            //log.info("TxStatus: {}, Executor: {}", txStatus, executorName); //Logging for test
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
