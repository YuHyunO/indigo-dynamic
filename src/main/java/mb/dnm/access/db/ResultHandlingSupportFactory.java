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


/**
 * {@link ResultHandlingSupport} 객체를 생성하는 Factort 객체이다.
 *
 * @author Yuhyun O
 */
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
    private boolean enforcePassTransactionToContexts = true;

    /**
     * fetch size 를 가져온다.
     *
     * @return the fetch size
     */
    public int getFetchSize() {
        return fetchSize;
    }

    /**
     * DB 조회결과를 fetch 할 사이즈를 설정한다.
     *
     * @param fetchSize the fetch size
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    /**
     * fetchedInputName 을 가져온다.
     *
     * @return the fetched input name
     */
    public String getFetchedInputName() {
        return fetchedInputName;
    }

    /**
     * fetchSize 만큼 조회된 결과를 {@link IterationGroup}에 Input 하여 처리할 때, 어떤 파라미터명으로 input 할 지에 대한 이름이다.
     *
     * @param fetchedInputName the fetched input name
     */
    public void setFetchedInputName(String fetchedInputName) {
        this.fetchedInputName = fetchedInputName;
    }

    /**
     * {@code ResultHandlingProcessor}, 즉 등록된 {@link IterationGroup} 을 가져온다.
     *
     * @return the result handling processor
     */
    public IterationGroup getResultHandlingProcessor() {
        return resultHandlingProcessor;
    }

    /**
     * fetchedSize 만큼 fetch 되는 DB 조회 결과를 처리할 {@code ResultHandlingProcessor}, 즉 등록된 {@link IterationGroup} 을 등록한다.
     *
     * @param resultHandlingProcessor the result handling processor
     */
    public void setResultHandlingProcessor(IterationGroup resultHandlingProcessor) {
        this.resultHandlingProcessor = resultHandlingProcessor;
    }

    /**
     * Is enforce pass transaction to contexts boolean.
     *
     * @return the boolean
     */
    public boolean isEnforcePassTransactionToContexts() {
        return enforcePassTransactionToContexts;
    }

    /**
     * {@code ResultHandlingProcessor}, 즉 {@link IterationGroup}에서 Service-Chaining을 할 때 DB 트랜잭션을 일관되게 유지할 지에 대한 설정이다.
     *
     * @param enforcePassTransactionToContexts the enforce pass transaction to contexts
     */
    public void setEnforcePassTransactionToContexts(boolean enforcePassTransactionToContexts) {
        this.enforcePassTransactionToContexts = enforcePassTransactionToContexts;
    }

    /**
     * 새로운 {@link ResultHandlingSupport}를 생성 후 반환한다.
     *
     * @param ctx the ctx
     * @return the result handling support
     */
    public ResultHandlingSupport getResultHandlingSupport(ServiceContext ctx) {
        log.debug("Creating a new ResultHandlingSupport object[fetchSize: {}, fetchedInputName: {}]", fetchSize, fetchedInputName);
        ResultHandlingSupport support = new ResultHandlingSupport(ctx);
        support.setFetchSize(fetchSize);
        support.setFetchedInputName(fetchedInputName);
        support.setEnforcePassTransactionToContexts(enforcePassTransactionToContexts);

        if (resultHandlingProcessor != null) {
            resultHandlingProcessor.setInput(fetchedInputName);
            resultHandlingProcessor.setIterationInputName(fetchedInputName);
            resultHandlingProcessor.setPassTransactionToContexts(enforcePassTransactionToContexts);

            support.setResultHandlingProcessor(resultHandlingProcessor);
        }
        return support;
    }


}
