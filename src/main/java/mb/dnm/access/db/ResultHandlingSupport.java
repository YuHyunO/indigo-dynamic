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

//<!>ResultHandlingSupport 를 static 한 객체로 사용하지 말 것

@Slf4j
@Getter
public class ResultHandlingSupport implements Serializable {
    private static final long serialVersionUID = -8192154600822429283L;
    private int fetchSize = 1;
    private int totalFetchedCount = 0;
    private int currentBufferSize = 0;
    private List<Map<String, Object>> resultSetBuffer;
    private final ServiceContext context;
    private final int currentQueryOrder;
    private final int currentErrorQueryOrder;
    private final int currentDynamicCodeOrder;
    private final int currentErrorDynamicCodeOrder;
    @Setter
    /**
     * resultSetBuffer 에서 flushBuffer 를 통해 Fetch 되는 값들이  내부 services가 실행될 때 어떤 파라미터명으로 input 될 지에 대한 설정이다.
     * */
    private String fetchedInputName = "$RESULT_HANDLING_BUFFER";
    private IterationGroup resultHandlingProcessor;

    ResultHandlingSupport(ServiceContext context) {
        this.context = context;
        this.currentQueryOrder = context.getCurrentQueryOrder();
        this.currentErrorQueryOrder = context.getCurrentErrorQueryOrder();
        this.currentDynamicCodeOrder = context.getCurrentDynamicCodeOrder();
        this.currentErrorDynamicCodeOrder = context.getCurrentErrorDynamicCodeOrder();

        resultSetBuffer = new ArrayList<>();
    }


    private int executeInternal() {
        int fetched = resultSetBuffer.size();
        if (resultHandlingProcessor != null) {
            try {
                context.addContextParam(fetchedInputName, resultSetBuffer);
                //ResultHandlingSupport 에서 사용되는 IterationGroup의 Input 과 IterationInputName 을 fetchedInputName으로 강제하였음
                resultHandlingProcessor.setInput(fetchedInputName);
                resultHandlingProcessor.setIterationInputName(fetchedInputName);
                resultHandlingProcessor.setFetchSize(fetchSize);

                //매번의 반복문에서 같은 QueryOrder 를 지정해준다.
                context.setCurrentQueryOrder(currentQueryOrder);
                context.setCurrentErrorQueryOrder(currentErrorQueryOrder);
                context.setCurrentDynamicCodeOrder(currentDynamicCodeOrder);
                context.setCurrentErrorDynamicCodeOrder(currentErrorDynamicCodeOrder);

                resultHandlingProcessor.process(context);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            return fetched;
        } else {
            return 0;
        }
    }

    void fillResult(ResultContext<? extends Map<String, Object>> resultContext) {
        if (currentBufferSize >= fetchSize) {
            throw new IllegalStateException("Result buffer is full. Please call flushBuffer(ServiceContext) first.");
        }
        Map<String, Object> resultRow = resultContext.getResultObject();
        resultSetBuffer.add(resultRow);
        ++currentBufferSize;
    }

    boolean isBufferFull() {
        return currentBufferSize == fetchSize;
    }

    int flushBuffer() {
        try {
            if (!resultSetBuffer.isEmpty()) {
                log.debug("Flushing result set buffer. current buffer size: {}", currentBufferSize);
                int fetchedCount = executeInternal();
                totalFetchedCount += fetchedCount;
                resultSetBuffer = new ArrayList<>();
                return fetchedCount;
            } else {
                return 0;
            }
        } finally {
            currentBufferSize = 0;
        }
    }

    void setFetchSize(int fetchSize) {
        if (fetchSize < 1) {
            fetchSize = 1;
        }
        this.fetchSize = fetchSize;
    }

    ResultHandler<Map<String, Object>> getHandler() {
        return new BufferedResultHandler();
    }

    class BufferedResultHandler implements ResultHandler<Map<String, Object>> {

        @Override
        public void handleResult(ResultContext<? extends Map<String, Object>> resultContext) {
            if (!isBufferFull()) {
                fillResult(resultContext);
            } else {
                //ResultSet 버퍼가 가득차면 버퍼를 비운뒤 다시 버퍼를 채운다.
                flushBuffer();
                fillResult(resultContext);
            }
        }

    }

    void setResultHandlingProcessor(IterationGroup resultHandlingProcessor) {
        if (resultHandlingProcessor == null) {
            return;
        }
        this.resultHandlingProcessor = resultHandlingProcessor;
    }
}
