package mb.dnm.service.db;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;

import java.io.Serializable;
import java.util.Set;


/**
 * 여러 DB 작업을 하나의 트랜잭션으로 설정한다.<br>
 * 이미 존재하는 트랜잭션에 대해 중복으로 설정되지 않는다.
 * <br>
 * <br>
 *<pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;!-- 인터페이스가 사용하는 모든 트랜잭션을 grouping --&gt;
 * &lt;bean class="mb.dnm.service.db.StartTransaction"/&gt;
 *
 * &lt;!-- Alias로 지정된 Database의 트랜잭션만 grouping --&gt;
 * &lt;bean class="mb.dnm.service.db.StartTransaction"&gt;
 *     &lt;property name="sourceAlias"              value="<span style="color: black; background-color: #FAF3D4;">DB source alias</span>"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @see StartTransaction
 * @see EndTransaction
 * @see Rollback
 * @see Select
 * @see Insert
 * @see Delete
 * @see Update
 * @see CallProcedure
 *
 * */
@Slf4j
public class StartTransaction extends SourceAccessService implements Serializable {
    private static final long serialVersionUID = 6387073454432906415L;

    @Override
    public void process(ServiceContext ctx) {
        String txId = ctx.getTxId();
        InterfaceInfo info = ctx.getInfo();
        Set<String> executorNames = info.getExecutorNames();
        if (executorNames == null || executorNames.size() == 0) {
            throw new InvalidServiceConfigurationException(StartTransaction.class, "There is no query sequences which contains the information of an Executor.");
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

            boolean result = ctx.setGroupTransaction(executorName, true);
            if (result) {
                log.info("[{}]A TranscationContext is ready for the executor: {}. Group transaction is enabled.", txId, executorName);
            } else {
                log.info("[{}]A TranscationContext of the executor: {} is already exist. Group transaction is enabled.", txId, executorName);
            }
        }

    }

}
