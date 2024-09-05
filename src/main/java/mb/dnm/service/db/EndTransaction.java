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

        boolean errorOccurred = false;
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
                txContextMap.remove(executorName);
            }
        }

        if (errorOccurred) {
            throw error;
        }
    }
}
