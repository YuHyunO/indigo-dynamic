package mb.dnm.access.db;

import mb.dnm.core.context.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.Method;

@Slf4j
public class TransactionProxyInterceptor implements MethodInterceptor {

    private final QueryExecutor target;

    public TransactionProxyInterceptor(QueryExecutor target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        String methodName = method.getName();
        if (!methodName.startsWith("do") || methodName.equals("doSelect"))
            return methodProxy.invoke(proxy, args);
        if (args[0] == null)
            return methodProxy.invoke(proxy, args);


        TransactionContext txCtx = null;
        DataSourceTransactionManager txManager = null;
        TransactionStatus txStatus = null;
        try {
            txCtx = (TransactionContext) args[0];
            txManager = DataSourceProvider.access().getTransactionManager(txCtx.getName());

            if (txCtx.isGroupTxEnabled()
                    && !txStatus.isCompleted()) { //트랜잭션이 그룹으로 묶인 경우, 명시적인 commit 또는 rollback 명령이 있기전까지 하나의 트랜잭션을 유지

                log.info("[TX]Create a new transaction for executor: {}", txCtx.getName());
                txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
                txCtx.setTransactionStatus(txStatus);

                return methodProxy.invoke(target, args);

            } else { //트랜잭션 그룹이 지정되지 않은 경우, QueryExecutor 의 do*(*) 메소드 실행 시 마다 트랜잭션이 생성되고, 메소드 종료 시 commit 또는 rollback 됨
                txStatus = txManager.getTransaction(new DefaultTransactionDefinition());
                Object rtVal = methodProxy.invoke(target, args);
                txManager.commit(txStatus);
                return rtVal;
            }
        } catch (Throwable t) {
            if (txStatus != null && !txStatus.isCompleted()) {
                log.warn("[TX]Processing rollback this transaction. Executor name: {}, Query history: {}", txCtx.getName(), txCtx.getQueryHistory());
                txManager.rollback(txStatus);
                log.warn("[TX]Rollback completed. Executor name: {}", txCtx.getName());
                txCtx.setError(t);
                log.error("", t);
            }
            throw t;
        }
    }

}
