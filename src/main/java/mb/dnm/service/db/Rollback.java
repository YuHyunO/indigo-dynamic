package mb.dnm.service.db;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
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
public class Rollback extends SourceAccessService implements Serializable {
    private static final long serialVersionUID = -6914562194164795885L;

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

        Object constantExecutors = ctx.getContextParam("$constant_executor");

        for (String executorName : executorNames) {
            if (targetSourceName != null) {
                if (!executorName.equals(targetSourceName)) {
                    continue;
                }
            }

            if (constantExecutors instanceof Set) {
                if ( ((Set) constantExecutors).contains(executorName) )
                    continue;
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
                log.warn("[{}]Can not rollback. The transaction named '{}' is already completed.", txId, executorName);
                continue;
            }
            TransactionContext.LastTransactionStatus lastTxStatus = txContext.getLastTxStatus();
            DataSourceTransactionManager txManager = DataSourceProvider.access().getTransactionManager(executorName);
            Object key = txManager.getDataSource();
            log.trace("[{}]Retrieved the transaction manager [{}] managing the dataSource [{}]. Current executor name is [{}]", txId, txManager, key, executorName);
            ConnectionHolder conHolder = null;
            Connection connection = null;

            try {
                Object conHolderObj = TransactionSynchronizationManager.getResource(key);
                if (conHolderObj == null) {
                    log.debug("[{}]The ConnectionHolder object of the executor [{}] is null. No transaction to end.", txId, executorName);
                    continue;
                }

                conHolder = (ConnectionHolder) conHolderObj;
                ConnectionHandle conHandle = conHolder.getConnectionHandle();
                if (conHandle == null) {
                    log.debug("[{}]There is no JDBC connection of the executor[{}] to release", txId, executorName);
                    continue;
                }

                connection = conHandle.getConnection();
                if (connection.isClosed()) {
                    log.debug("[{}]The JDBC connection of the executor[{}] is already closed", txId, executorName);
                    continue;
                }

                String currentTxName = TransactionSynchronizationManager.getCurrentTransactionName();
                log.trace("[{}]The current transaction name is [{}]", txId, currentTxName);
                if (!(currentTxName != null
                        && currentTxName.equals(executorName))) {
                    DefaultTransactionDefinition def = txContext.getTransactionDefinition();
                    if (lastTxStatus.isInitialized()) {
                        TransactionSynchronizationManager.setCurrentTransactionName(lastTxStatus.getCurrentTransactionName());
                        TransactionSynchronizationManager.setCurrentTransactionReadOnly(lastTxStatus.isCurrentTransactionReadOnly());
                        TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(lastTxStatus.getCurrentTransactionIsolationLevel());
                    } else {
                        TransactionSynchronizationManager.setCurrentTransactionName(def.getName());
                        TransactionSynchronizationManager.setCurrentTransactionReadOnly(def.isReadOnly());
                        TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(def.getIsolationLevel());
                    }
                    if (TransactionSynchronizationManager.isSynchronizationActive()) {
                        TransactionSynchronizationManager.clearSynchronization();
                    }
                    TransactionSynchronizationManager.initSynchronization();
                    List<TransactionSynchronization> synchs = lastTxStatus.getSynchronizations();
                    if (synchs != null) {
                        for (TransactionSynchronization txSync : lastTxStatus.getSynchronizations()) {
                            TransactionSynchronizationManager.registerSynchronization(txSync);
                            log.trace("[{}]Registering synchronization [{}]", txId, txSync);
                        }
                    }
                }
                log.trace("[{}]\n{}", txId, lastTxStatus.toString());
                TransactionSynchronizationManager.setActualTransactionActive(true);

                log.info("[{}]Rollback transaction. executor: {}", txId, executorName);
                txManager.rollback(txStatus);
                log.info("[{}]Transaction rollbacked. executor: {}", txId, executorName);
            } catch (Throwable t) {
                log.error("[" + txId + "]Commit failed. Processed rollback for executor: " + executorName + ". Cause: ", t);
                throw t;
            } finally {
                txContextMap.remove(executorName); //Commit 이나 Rollback 처리를 한 뒤 항상 작업한 DataSource와 관련된 TransactionContext 객체를 지워줌
                if (key != null) {
                    Object unboundResource = TransactionSynchronizationManager.unbindResourceIfPossible(key);
                    if (unboundResource != null) {
                        log.trace("[{}]The resource[{}] is removed by the '{}' service", txId, unboundResource, this.getClass().getSimpleName());
                    }
                }

                TransactionSynchronizationManager.setCurrentTransactionName((String) null);
                TransactionSynchronizationManager.setCurrentTransactionIsolationLevel((Integer) null);
                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    TransactionSynchronizationManager.clearSynchronization();
                }
                TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
                TransactionSynchronizationManager.setActualTransactionActive(false);
                try {
                    if (connection != null && !connection.isClosed()) {
                        log.warn("[{}]The JDBC connection of the executor[{}] is not closed by TransactionManager", txId, executorName);
                        conHolder.released();
                        conHolder.clear();
                    }
                } catch (Throwable t) {
                    log.error("", t);
                }
            }
        }

    }
}
