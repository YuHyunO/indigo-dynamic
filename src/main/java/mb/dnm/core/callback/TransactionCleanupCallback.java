package mb.dnm.core.callback;

import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        ConnectionHolder conHolder = null;
        Connection connection = null;

        try {
            for (Map.Entry<String, TransactionContext> entry : txCtxMap.entrySet()) {
                String executorName = entry.getKey();
                TransactionContext txCtx = entry.getValue();
                TransactionStatus txStatus = txCtx.getTransactionStatus();
                TransactionContext.LastTransactionStatus lastTxStatus = txCtx.getLastTxStatus();


                if (txStatus != null) {
                    if (!txStatus.isCompleted()) {
                        DataSourceTransactionManager txManager = DataSourceProvider.access().getTransactionManager(executorName);
                        Object key = txManager.getDataSource();

                        Object conHolderObj = TransactionSynchronizationManager.getResource(key);
                        if (conHolderObj == null) {
                            log.debug("The ConnectionHolder object of the executor [{}] is null. No transaction to end.", executorName);
                            continue;
                        }

                        conHolder = (ConnectionHolder) conHolderObj;
                        ConnectionHandle conHandle = conHolder.getConnectionHandle();
                        if (conHandle == null) {
                            log.debug("There is no JDBC connection of the executor[{}] to release", executorName);
                            continue;
                        }

                        connection = conHandle.getConnection();
                        if (connection.isClosed()) {
                            log.debug("The JDBC connection of the executor[{}] is already closed", executorName);
                            continue;
                        }

                        /*if (TransactionSynchronizationManager.getCurrentTransactionName() != null &&
                                !TransactionSynchronizationManager.getCurrentTransactionName().equals(executorName)) {
                            if (lastTxStatus.isInitialized()) {
                                TransactionSynchronizationManager.setCurrentTransactionName(lastTxStatus.getCurrentTransactionName());
                                TransactionSynchronizationManager.setCurrentTransactionReadOnly(lastTxStatus.isCurrentTransactionReadOnly());
                                TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(lastTxStatus.getCurrentTransactionIsolationLevel());
                                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                                    TransactionSynchronizationManager.clearSynchronization();
                                }
                                TransactionSynchronizationManager.initSynchronization();
                                List<TransactionSynchronization> synchs = lastTxStatus.getSynchronizations();
                                for (TransactionSynchronization txSync : synchs) {
                                    TransactionSynchronizationManager.registerSynchronization(txSync);
                                }
                                TransactionSynchronizationManager.setActualTransactionActive(true);
                            }
                        }*/
                        if (lastTxStatus.isInitialized()) {
                            TransactionSynchronizationManager.setCurrentTransactionName(lastTxStatus.getCurrentTransactionName());
                            TransactionSynchronizationManager.setCurrentTransactionReadOnly(lastTxStatus.isCurrentTransactionReadOnly());
                            TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(lastTxStatus.getCurrentTransactionIsolationLevel());
                        } else {
                            DefaultTransactionDefinition def = txCtx.getTransactionDefinition();
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
                                log.trace("Registering synchronization [{}] to release transaction", txSync);
                            }
                        }
                        TransactionSynchronizationManager.setActualTransactionActive(true);


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

        } catch (Throwable t) {
            log.error("", t);
        } finally {
            txCtxMap.clear();
            try {
                if (connection != null && !connection.isClosed()) {
                    conHolder.released();
                    conHolder.clear();
                }
            } catch (Throwable t) {
                log.error("", t);
            }
        }


        try {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.setCurrentTransactionIsolationLevel((Integer) null);
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
            TransactionSynchronizationManager.setCurrentTransactionName((String) null);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.clearSynchronization();
            }

            Map<Object, Object> resourceMap = TransactionSynchronizationManager.getResourceMap();
            if (resourceMap != null) {
                List<Object> keys = new ArrayList<>(resourceMap.keySet());
                for (Object key : keys) {
                    TransactionSynchronizationManager.unbindResourceIfPossible(key);
                }
            }
        } catch (Exception e) {
            log.error("[{}]", ctx.getTxId(), e);
        }
    }

}
