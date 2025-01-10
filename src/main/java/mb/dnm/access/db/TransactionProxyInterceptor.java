package mb.dnm.access.db;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.TransactionContext;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;


/**
 * Transaction 을 관리하기 위한 proxy 클래스
 * QueryExecutor에서 메소드명이 do 로 시작되는 메소드는 이 proxy 클래스에 의해 관리된다.
 * <br>
 * <code>QueryExecutor</code> 의 <code>do*(**)</code> 메소드와 관련해서만 proxy 로 실행됨
 *
 * @author Yuhyun O
 * @version 2024.09.05
 * @see QueryExecutor
 * @see DataSourceProvider
 * @see TransactionContext
 */
@Slf4j
public class TransactionProxyInterceptor implements MethodInterceptor, Serializable {

    private static final long serialVersionUID = -6562784215800920896L;
    private final QueryExecutor target;

    /**
     * Instantiates a new Transaction proxy interceptor.
     *
     * @param target the target
     */
    public TransactionProxyInterceptor(QueryExecutor target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        String methodName = method.getName();

        //메소드명이 do로 시작되지 않는 경우 그냥 실행
        if (!methodName.startsWith("do")) {
            return methodProxy.invoke(target, args);
        }

        //args[1] = TransactionContext 객체가 null 인 경우 그냥 실행
        if (args[0] == null) {
            log.debug("Executing query with id '{}'", args[1]);
            return methodProxy.invoke(target, args);
        }

        TransactionContext txCtx = null;
        DataSourceTransactionManager txManager = null;
        TransactionStatus txStatus = null;
        String executorName = null;
        TransactionContext.LastTransactionStatus lastTxStatus = null;
        //Object dataSource = null;
        try {
            txCtx = (TransactionContext) args[0];
            txManager = DataSourceProvider.access().getTransactionManager(txCtx.getName());
            executorName = txCtx.getName();
            //dataSource = txManager.getDataSource();

            if (txCtx.isGroupTxEnabled()) {//트랜잭션이 그룹으로 묶인 경우, 명시적인 commit 또는 rollback 명령이 있기전까지 하나의 트랜잭션을 유지
                lastTxStatus = txCtx.getLastTxStatus();
                txStatus = txCtx.getTransactionStatus();

                //TransactionSynchronizationManager 는 ThreadLocal 변수를 사용하여 트랜잭션을 관리하므로 여러 트랜잭션을 하나의 Thread 에서 사용하는 경우 관리가 필요함
                String currentTxName = TransactionSynchronizationManager.getCurrentTransactionName();
                if (!(currentTxName != null && currentTxName.equals(executorName))) {

                    if (lastTxStatus.isInitialized()) {
                        TransactionSynchronizationManager.setCurrentTransactionName(lastTxStatus.getCurrentTransactionName());
                        TransactionSynchronizationManager.setCurrentTransactionReadOnly(lastTxStatus.isCurrentTransactionReadOnly());
                        TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(lastTxStatus.getCurrentTransactionIsolationLevel());
                        TransactionSynchronizationManager.setActualTransactionActive(lastTxStatus.isActualTransactionActive());
                        if (TransactionSynchronizationManager.isSynchronizationActive()) {
                            TransactionSynchronizationManager.clearSynchronization();
                        }
                        TransactionSynchronizationManager.initSynchronization();
                        for (TransactionSynchronization txSync : lastTxStatus.getSynchronizations()) {
                            TransactionSynchronizationManager.registerSynchronization(txSync);
                        }
                    } else {
                        clearTransaction(null);
                    }
                }

                DefaultTransactionDefinition txDef = txCtx.getTransactionDefinition();
                if (txDef == null) {
                    log.info("[TX]A group transaction for executor: {} is assigned.", executorName);
                    txDef = new DefaultTransactionDefinition();
                    txDef.setName(executorName);
                    txDef.setTimeout(txCtx.getTimeoutSecond());
                    txCtx.setTransactionDefinition(txDef);
                    txStatus = txManager.getTransaction(txDef);
                    txCtx.setTransactionStatus(txStatus);
                } else {
                    log.info("[TX]Continuing with group transaction for executor: {}", executorName);
                }

                txCtx.setLastTransactionStatus();
                log.debug("Executing query with id '{}'", args[1]);
                Object rtVal = methodProxy.invoke(target, args);

                return rtVal;
            } else { //트랜잭션 그룹이 지정되지 않은 경우, QueryExecutor 의 do*(**) 메소드 실행 시 마다 트랜잭션이 생성되고, 메소드 종료 시 commit 또는 rollback 됨

                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
                txDef.setName(executorName);
                txDef.setTimeout(txCtx.getTimeoutSecond());
                txStatus = txManager.getTransaction(txDef);

                txCtx.setLastTransactionStatus();
                lastTxStatus = txCtx.getLastTxStatus();

                log.debug("Executing query with id '{}'", args[1]);
                Object rtVal = methodProxy.invoke(target, args);

                if (!txStatus.isCompleted()) {
                    if (txCtx.isConstant()) {
                        Object key = txManager.getDataSource();
                        ConnectionHolder conHolder = null;
                        Connection connection = null;
                        Object conHolderObj = TransactionSynchronizationManager.getResource(key);
                        if (conHolderObj == null) {
                            log.debug("The ConnectionHolder object of the executor [{}] is null. No transaction to commit.", executorName);
                            return rtVal;
                        }
                        conHolder = (ConnectionHolder) conHolderObj;
                        ConnectionHandle conHandle = conHolder.getConnectionHandle();
                        if (conHandle == null) {
                            log.debug("There is no JDBC connection of the executor[{}] to release", executorName);
                            return rtVal;
                        }
                        connection = conHandle.getConnection();
                        if (connection.isClosed()) {
                            log.debug("The JDBC connection of the executor[{}] is already closed", executorName);
                            return rtVal;
                        }
                        String currentTxName = TransactionSynchronizationManager.getCurrentTransactionName();
                        log.debug("The current transaction name is [{}]", currentTxName);
                        if (!(executorName.equals(currentTxName))) {
                            if (lastTxStatus.isInitialized()) {
                                TransactionSynchronizationManager.setCurrentTransactionName(lastTxStatus.getCurrentTransactionName());
                                TransactionSynchronizationManager.setCurrentTransactionReadOnly(lastTxStatus.isCurrentTransactionReadOnly());
                                TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(lastTxStatus.getCurrentTransactionIsolationLevel());
                            } else {
                                TransactionSynchronizationManager.setCurrentTransactionName(txDef.getName());
                                TransactionSynchronizationManager.setCurrentTransactionReadOnly(txDef.isReadOnly());
                                TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(txDef.getIsolationLevel());
                            }
                            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                                TransactionSynchronizationManager.clearSynchronization();
                            }
                            TransactionSynchronizationManager.initSynchronization();
                            List<TransactionSynchronization> synchs = lastTxStatus.getSynchronizations();
                            if (synchs != null) {
                                for (TransactionSynchronization txSync : lastTxStatus.getSynchronizations()) {
                                    TransactionSynchronizationManager.registerSynchronization(txSync);
                                }
                            }
                        }
                        TransactionSynchronizationManager.setActualTransactionActive(true);
                    }
                    log.debug("Committing non group transaction '{}'", executorName);
                    txManager.commit(txStatus);
                }
                return rtVal;
            }
        } catch (Throwable t) {

            if (txStatus != null && !txStatus.isCompleted()) {
                if (lastTxStatus != null) {
                    String currentTxName = TransactionSynchronizationManager.getCurrentTransactionName();
                    if (!(currentTxName != null && currentTxName.equals(executorName))) {
                        if (lastTxStatus.isInitialized()) {
                            TransactionSynchronizationManager.setCurrentTransactionName(lastTxStatus.getCurrentTransactionName());
                            TransactionSynchronizationManager.setCurrentTransactionReadOnly(lastTxStatus.isCurrentTransactionReadOnly());
                            TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(lastTxStatus.getCurrentTransactionIsolationLevel());
                            TransactionSynchronizationManager.setActualTransactionActive(lastTxStatus.isActualTransactionActive());
                            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                                TransactionSynchronizationManager.clearSynchronization();
                            }
                            TransactionSynchronizationManager.initSynchronization();
                            List<TransactionSynchronization> syncs = lastTxStatus.getSynchronizations();
                            if (syncs != null) {
                                for (TransactionSynchronization txSync : lastTxStatus.getSynchronizations()) {
                                    TransactionSynchronizationManager.registerSynchronization(txSync);
                                }
                            }
                        }
                    }
                }


                log.warn("[TX]Processing rollback this transaction. Executor name: {}, Query history: {}", txCtx.getName(), txCtx.getQueryHistory());
                try {
                    txManager.rollback(txStatus);
                    log.warn("[TX]Rollback completed. Executor name: {}", txCtx.getName());
                } catch (Throwable t2) {
                    log.warn("[TX]Rollback failed. But the transaction will be cleared. Executor name: {}. Cause: {}"
                            , txCtx.getName(), t2.getMessage());
                }
                txCtx.setError(t);
            }
            throw t;
        } finally {
            if (!txCtx.isGroupTxEnabled()) {
                txCtx.setTransactionStatus(null);
                clearTransaction(null);
                //clearTransaction(dataSource);
                log.debug("[TX]Cleaned up non-group TransactionStatus");
            } else {
                String currentTxName = TransactionSynchronizationManager.getCurrentTransactionName();
                if (currentTxName != null && currentTxName.equals(executorName)) {
                    txCtx.setLastTransactionStatus();
                }
                log.trace("[TX]Last transaction status of the executor[{}] :\n{}", txCtx.getName(), txCtx.getLastTxStatus());
            }
        }
    }

    /**
     * Clear transaction.
     *
     * @param key the key
     */
    void clearTransaction(Object key) {

        if (key != null) {
            ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(key);
            DataSourceUtils.releaseConnection(conHolder.getConnection(), (DataSource) key);

            /*Object unbindedResource = TransactionSynchronizationManager.unbindResourceIfPossible(key);
            if (unbindedResource != null) {
                log.debug("The resource[{}] is removed by the '{}'", unbindedResource, this.getClass());
            } else {
                log.debug("The resource is null. Couldn't remove");
            }*/
        }

        TransactionSynchronizationManager.setCurrentTransactionName((String) null);
        TransactionSynchronizationManager.setCurrentTransactionIsolationLevel((Integer) null);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

}
