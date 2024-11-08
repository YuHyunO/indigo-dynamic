package mb.dnm.access.db;

import mb.dnm.core.context.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.lang.reflect.Method;


/**
 *
 * 어댑터 자체적으로 Transaction을 관리하기 위한 클래스
 * <br>
 * <code>QueryExecutor</code> 의 <code>do*(**)</code> 메소드와 관련해서만 proxy 로 실행됨
 * 
 * @see QueryExecutor
 * @see DataSourceProvider
 * @see TransactionContext
 *
 * @author Yuhyun O
 * @version 2024.09.05
 *
 * */
@Slf4j
public class TransactionProxyInterceptor implements MethodInterceptor, Serializable {

    private static final long serialVersionUID = -6562784215800920896L;
    private final QueryExecutor target;

    public TransactionProxyInterceptor(QueryExecutor target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        String methodName = method.getName();
        if (!methodName.startsWith("do")) {
            log.debug("Executing query with id '{}'", args[1]);
            return methodProxy.invoke(target, args);
        }

        if (args[0] == null) {//TransactionContext 객체가 null 인 경우
            log.debug("Executing query with id '{}'", args[1]);
            return methodProxy.invoke(target, args);
        }

        TransactionContext txCtx = null;
        DataSourceTransactionManager txManager = null;
        TransactionStatus txStatus = null;

        try {
            txCtx = (TransactionContext) args[0];
            txManager = DataSourceProvider.access().getTransactionManager(txCtx.getName());
            String executorName = txCtx.getName();

            if (txCtx.isGroupTxEnabled()) { //트랜잭션이 그룹으로 묶인 경우, 명시적인 commit 또는 rollback 명령이 있기전까지 하나의 트랜잭션을 유지

                txStatus = txCtx.getTransactionStatus();
                if (txStatus == null) { //TransactionStatus가 존재하는 경우 쿼리작업을 하려는 dataSource에 대한 트랜잭션을 새로 생성하지 않음
                    log.info("[TX]A group transaction for executor: {} is assigned.", executorName);

                    DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
                    txDef.setName(executorName);
                    txDef.setTimeout(txCtx.getTimeoutSecond());
                    txStatus = txManager.getTransaction(txDef);
                    txCtx.setTransactionDefinition(txDef);
                    txCtx.setTransactionStatus(txStatus);
                } else {
                    log.info("[TX]Continuing with group transaction for executor: {}", executorName);
                }
                log.debug("Executing query with id '{}'", args[1]);
                Object rtVal = methodProxy.invoke(target, args);

                if (log.isTraceEnabled()) {
                    boolean actualTxActive = TransactionSynchronizationManager.isActualTransactionActive();
                    boolean syncActive = TransactionSynchronizationManager.isSynchronizationActive();
                    String curTxName = TransactionSynchronizationManager.getCurrentTransactionName();
                    StringBuilder sb = new StringBuilder();
                    if (txStatus != null) {
                        DefaultTransactionStatus dfStatus = (DefaultTransactionStatus) txStatus;
                        sb.append("[")
                                .append("isNewTransaction: ")
                                .append(dfStatus.isNewTransaction())
                                .append(", isNewSynchronization: ")
                                .append(dfStatus.isNewSynchronization())
                                .append(", isCompleted: ")
                                .append(dfStatus.isCompleted())
                                .append("]");
                    } else {
                        sb.append("[null]");
                    }

                    log.trace("EndTransaction-[executor name: {}, isActualTransactionActive: {}, isSynchronizationActive: {}, TransactionStatus: {}, Current transaction name: {}]"
                            , executorName, actualTxActive, syncActive, sb, curTxName);
                }
                return rtVal;
            } else { //트랜잭션 그룹이 지정되지 않은 경우, QueryExecutor 의 do*(**) 메소드 실행 시 마다 트랜잭션이 생성되고, 메소드 종료 시 commit 또는 rollback 됨

                DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
                txDef.setName(executorName);
                txDef.setTimeout(txCtx.getTimeoutSecond());
                txStatus = txManager.getTransaction(txDef);
                log.debug("Executing query with id '{}'", args[1]);
                Object rtVal = methodProxy.invoke(target, args);

                if (log.isTraceEnabled()) {
                    boolean actualTxActive = TransactionSynchronizationManager.isActualTransactionActive();
                    boolean syncActive = TransactionSynchronizationManager.isSynchronizationActive();
                    String curTxName = TransactionSynchronizationManager.getCurrentTransactionName();
                    StringBuilder sb = new StringBuilder();
                    if (txStatus != null) {
                        DefaultTransactionStatus dfStatus = (DefaultTransactionStatus) txStatus;
                        sb.append("[")
                                .append("isNewTransaction: ")
                                .append(dfStatus.isNewTransaction())
                                .append(", isNewSynchronization: ")
                                .append(dfStatus.isNewSynchronization())
                                .append(", isCompleted: ")
                                .append(dfStatus.isCompleted())
                                .append("]");
                    } else {
                        sb.append("[null]");
                    }

                    log.trace("EndTransaction-[executor name: {}, isActualTransactionActive: {}, isSynchronizationActive: {}, TransactionStatus: {}, Current transaction name: {}]"
                            , executorName, actualTxActive, syncActive, sb, curTxName);
                }

                if (!txStatus.isCompleted()) {
                    txManager.commit(txStatus);
                }
                return rtVal;

            }
        } catch (Throwable t) {
            if (txStatus != null && !txStatus.isCompleted()) {
                log.warn("[TX]Processing rollback this transaction. Executor name: {}, Query history: {}", txCtx.getName(), txCtx.getQueryHistory());
                txManager.rollback(txStatus);
                log.warn("[TX]Rollback completed. Executor name: {}", txCtx.getName());
                txCtx.setError(t);
            }
            throw t;
        } finally {
            if (!txCtx.isGroupTxEnabled()) {
                txCtx.setTransactionStatus(null);
                log.debug("[TX]Cleaned up non-group TransactionStatus");
            }
        }
    }

}
