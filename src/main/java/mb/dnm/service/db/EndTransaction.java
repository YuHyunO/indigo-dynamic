package mb.dnm.service.db;

import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.util.List;
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
public class EndTransaction extends SourceAccessService implements Serializable {
    private static final long serialVersionUID = -4596639645344510758L;

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

        String targetSourceName = null;
        if (sourceName != null) {
            targetSourceName = sourceName;
        } else if (sourceAlias != null) {
            targetSourceName = getSourceName(info);
        }

        boolean errorOccurred = ctx.isErrorExist();
        for (String executorName : executorNames) {
            if (targetSourceName != null) {
                if (!executorName.equals(targetSourceName)) {
                    continue;
                }
            }

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
                log.warn("[{}]Can not end the transaction. The transaction named '{}' is already completed.", txId, executorName);
                continue;
            }

            DataSourceTransactionManager txManager = DataSourceProvider.access().getTransactionManager(executorName);
            try {
                DefaultTransactionStatus status = (DefaultTransactionStatus) txStatus;
                DefaultTransactionDefinition txDef = txContext.getTransactionDefinition();
                if (!TransactionSynchronizationManager.isSynchronizationActive() && status.isNewSynchronization()) {
                    TransactionSynchronizationManager.setActualTransactionActive(status.hasTransaction());
                    TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(txDef.getIsolationLevel());
                    TransactionSynchronizationManager.setCurrentTransactionReadOnly(txDef.isReadOnly());
                    TransactionSynchronizationManager.setCurrentTransactionName(txContext.getName());
                    TransactionSynchronizationManager.initSynchronization();
                }


                if (log.isTraceEnabled()) {
                    boolean actualTxActive = TransactionSynchronizationManager.isActualTransactionActive();
                    boolean syncActive = TransactionSynchronizationManager.isSynchronizationActive();
                    String curTxName = TransactionSynchronizationManager.getCurrentTransactionName();
                    StringBuilder sb = new StringBuilder();
                    sb.append("[")
                            .append("isNewTransaction: ")
                            .append(status.isNewTransaction())
                            .append(", isNewSynchronization: ")
                            .append(status.isNewSynchronization())
                            .append(", isCompleted: ")
                            .append(status.isCompleted())
                            .append(", hasTransaction: ")
                            .append(status.hasTransaction())
                            .append("]");

                    log.trace("EndTransaction-[executor name: {}, isActualTransactionActive: {}, isSynchronizationActive: {}, TransactionStatus: {}, Current transaction name: {}]"
                            , executorName, actualTxActive, syncActive, sb, curTxName);
                }

                if (errorOccurred) {
                    log.error("[" + txId + "]An error detected in the service context. Processing rollback for executor: " + executorName + ". Because of exception of before ");
                    txManager.rollback(txStatus);
                    continue;
                }

                log.info("[{}]Committing transaction for executor: {}", txId, executorName);
                txManager.commit(txStatus);
                log.info("[{}]Transaction committed for executor: {}", txId, executorName);
            } catch (Throwable t) {
                log.error("[" + txId + "]Commit failed. Processing rollback for executor: " + executorName + ". Cause: ", t);
                txManager.rollback(txStatus);
                throw t;
            } finally {
                //txContext.setTransactionStatus(null);
                txContextMap.remove(executorName); //Commit 이나 Rollback 처리를 한 뒤 항상 작업한 DataSource와 관련된 TransactionContext 객체를 지워줌
            }
        }
    }
}
