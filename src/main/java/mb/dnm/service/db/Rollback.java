package mb.dnm.service.db;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.storage.InterfaceInfo;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Map;
import java.util.Set;

/**
 * <code>StartTransaction</code> 에 의해 시작된 트랜잭션 Rollback 한다.<br>
 * 트랜잭션 그룹이 존재하지 않으면 이 서비스의 호출은 아무런 효과도 없다.<br>

 * @see StartTransaction
 * @see Select
 * @see Insert
 * @see Delete
 * @see Update
 * @see CallProcedure
 *
 * @author Yuhyun O
 * @version 2024.10.02
 *
 * */
@Slf4j
public class Rollback extends ParameterAssignableService {
    @Override
    public void process(ServiceContext ctx) throws Throwable {
        String txId = ctx.getTxId();
        InterfaceInfo info = ctx.getInfo();
        Set<String> executorNames = info.getExecutorNames();
        if (executorNames == null || executorNames.size() == 0) {
            throw new InvalidServiceConfigurationException(StartTransaction.class, "There is no query sequences which contains the information of an Executor.");
        }

        Map<String, TransactionContext> txContextMap = ctx.getTxContextMap();
        if (txContextMap == null) {
            log.warn("[{}]The transaction contexts do not exist for termination. It may have already been terminated or not created.", txId);
            return;
        }

        for (String executorName : executorNames) {
            TransactionContext txContext = txContextMap.get(executorName);
            if (txContext == null) {
                log.warn("[{}]The transaction context named '{}' does not exist for termination. It may have already been terminated or not created.", txId, executorName);
                continue;
            }
            TransactionStatus txStatus = txContext.getTransactionStatus();
            if (txStatus == null) {
                continue;
            }
            if (txStatus.isCompleted()) {
                log.warn("[{}]Can not rollback. The transaction named '{}' is already completed.", txId, executorName);
                continue;
            }

            DataSourceTransactionManager txManager = DataSourceProvider.access().getTransactionManager(executorName);
            try {
                log.info("[{}]Rollback transaction. executor: {}", txId, executorName);
                txManager.rollback(txStatus);
                log.info("[{}]Transaction rollbacked. executor: {}", txId, executorName);
            } catch (Throwable t) {
                log.error("[" + txId + "]Commit failed. Processed rollback for executor: " + executorName + ". Cause: ", t);
                throw t;
            } finally {
                txContextMap.remove(executorName); //Commit 이나 Rollback 처리를 한 뒤 항상 작업한 DataSource와 관련된 TransactionContext 객체를 지워줌
            }
        }

    }
}
