package mb.dnm.service.db;

import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.storage.InterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Map;
import java.util.Set;

/**
 * <code>StartTransaction</code> 에 의해 시작된 트랜잭션 그룹을 종료한다.<br>
 * 트랜잭션 그룹이 존재하지 않으면 이 서비스의 호출은 아무런 효과도 없다.<br>
 * Commit 또는 Rollback 의 수행되는 기준은 <code>EndTransaction</code> 서비스가 호출되는 시점에서 <code>ServiceContext</code> 에 에러 발생이력 존재여부를 따른다.
 * 에러 발생 이력이 존재하면 Rollback, 존재하지 않으면 Commit 된다.

 * @see StartTransaction
 * @see Select
 * @see Insert
 * @see Delete
 * @see Update
 * @see CallProcedure
 *
 * @author Yuhyun O
 * @version 2024.09.05
 *
 * */
@Slf4j
public class EndTransaction extends ParameterAssignableService {
    @Override
    public void process(ServiceContext ctx) throws Throwable {
        String txId = ctx.getTxId();
        InterfaceInfo info = ctx.getInfo();
        Set<String> executorNames = info.getExecutorNames();
        if (executorNames == null || executorNames.size() == 0) {
            throw new InvalidServiceConfigurationException(StartTransaction.class, "There is no query sequences which contains the information of an Executor.");
        }

        Map<String, TransactionContext> txContextMap = ctx.getTxContextMap();

        boolean errorOccurred = ctx.isErrorExist();
        Throwable error = null;
        for (String executorName : executorNames) {
            TransactionContext txContext = txContextMap.get(executorName);
            TransactionStatus txStatus = txContext.getTransactionStatus();
            if (txStatus == null) {
                continue;
            }
            DataSourceTransactionManager txManager = DataSourceProvider.access().getTransactionManager(executorName);
            try {
                if (errorOccurred) {
                    txManager.rollback(txStatus);
                    log.error("[" + txId + "]Commit failed. Processed rollback for executor: " + executorName + ". Because of exception of before ");
                    continue;
                }
                txManager.commit(txStatus);
                log.info("[{}]Transaction committed for executor: {}", txId, executorName);
            } catch (Throwable t) {
                error = t;
                errorOccurred = true;
                txManager.rollback(txStatus);
                log.error("[" + txId + "]Commit failed. Processed rollback for executor: " + executorName + ". Cause: ", t);
            } finally {
                txContextMap.remove(executorName); //Commit 이나 Rollback 처리를 한 뒤 항상 작업한 DataSource와 관련된 TransactionContext 객체를 지워줌
            }
        }

        if (errorOccurred) {
            throw error;
        }
    }
}
