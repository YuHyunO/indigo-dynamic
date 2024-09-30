package mb.dnm.service.db;

import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.storage.InterfaceInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;


/**
 * 새로운 Transaction Group 을 시작한다.<br>
 * 
 * 트랜잭션은 mb.dnm.service.db 패키지에 존재하는 서비스를 사용하는 경우에만 적용된다.
 * 각각의 <code>ServiceContext</code> 마다 트랜잭션이 적용되며, <code>StartTransaction</code> 서비스가 호촐되면
 * 트랜잭션이 존재하지 않는 경우는 새로운 트랜잭션을 시작하고 트랜잭션이 이미 존재하는 경우는 그 트랜잭션에 참여한다.<br><br>
 * 
 * 트랜잭션이 종료되는 시점은 다음과 같다.<br>
 * <i>
 *     1. <code>EndTransaction</code> 서비스가 호출됨으로써 Commit 또는 Rollback<br>
 *     2. 서비스 Chaining 증 Exception 발생 시 Rollback<br>
 *     3. 위 두 경우에 해당되지 않는 경우, 서비스 Chaining 종료 후 <code>TransactionCleanupCallback</code>에 의해 Commit 또는 Rollback
 * </i>
 * <br><br>
 *
 * <b>! Stored Procedure 또는 Function 내에 commit 이나 rollback 이 구문이 존재하는 경우는 트랜잭션 관리가 불가능하다.</b>
 *
 * @see EndTransaction
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
public class StartTransaction extends ParameterAssignableService {
    @Override
    public void process(ServiceContext ctx) {
        String txId = ctx.getTxId();
        InterfaceInfo info = ctx.getInfo();
        Set<String> executorNames = info.getExecutorNames();
        if (executorNames == null || executorNames.size() == 0) {
            throw new InvalidServiceConfigurationException(StartTransaction.class, "There is no query sequences which contains the information of an Executor.");
        }
        for (String executorName : executorNames) {
            boolean result = ctx.setGroupTransaction(executorName, true);
            if (result) {
                log.info("[{}]A TranscationContext is ready for the executor: {}", txId, executorName);
            } else {
                log.info("[{}]A TranscationContext of the executor: {} is already exist", txId, executorName);
            }
        }

    }
}
