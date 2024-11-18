package mb.dnm.access.db;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.general.IterationGroup;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter @Setter
public class ResultHandlingSupportFactory implements Serializable {
    private static final long serialVersionUID = -1221749759844814025L;
    private int fetchSize = 1;
    /**
     * resultSetBuffer 에서 flushBuffer 를 통해 Fetch 되는 값들이  내부 services가 실행될 때 어떤 파라미터명으로 input 될 지에 대한 설정이다.
     * */
    private String fetchedInputName;
    private IterationGroup resultHandlingProcessor;

    public ResultHandlingSupport getResultHandlingSupport(ServiceContext ctx) {
        log.debug("Creating a new ResultHandlingSupport object[fetchSize: {}, fetchedInputName: {}]", fetchSize, fetchedInputName);
        ResultHandlingSupport support = new ResultHandlingSupport(ctx);
        support.setFetchSize(fetchSize);
        support.setFetchedInputName(fetchedInputName);
        if (resultHandlingProcessor != null) {
            resultHandlingProcessor.setInput(fetchedInputName);
            resultHandlingProcessor.setIterationInputName(fetchedInputName);
            //resultHandlingProcessor.setCreateNewContextEachLoop(false);
            resultHandlingProcessor.setPassTransactionToContexts(true);
            support.setResultHandlingProcessor(resultHandlingProcessor);
        }
        return support;
    }


}
