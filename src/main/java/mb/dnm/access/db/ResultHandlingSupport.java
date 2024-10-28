package mb.dnm.access.db;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.core.ErrorHandler;
import mb.dnm.core.Service;
import mb.dnm.core.callback.AfterProcessCallback;
import mb.dnm.core.callback.SessionCleanupCallback;
import mb.dnm.core.callback.TransactionCleanupCallback;
import mb.dnm.core.context.ServiceContext;
import org.apache.ibatis.session.ResultContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Getter
public class ResultHandlingSupport {
    @Setter
    private int fetchSize = 1;
    private int currentBufferSize = 0;
    private List<Map<String, Object>> resultSetBuffer;
    @Setter
    private List<Service> services;
    @Setter
    private List<ErrorHandler> errorHandlers;
    private List<AfterProcessCallback> callbacks;

    public ResultHandlingSupport() {
        resultSetBuffer = new ArrayList<>();
    }

    public ResultHandlingSupport(List<Service> services, List<ErrorHandler> errorHandlers, List<AfterProcessCallback> callbacks) {
        this.services = services;
        this.errorHandlers = errorHandlers;
        setCallbacks(callbacks);
        resultSetBuffer = new ArrayList<>();
    }

    private int executeInternal(ServiceContext ctx) {

        return 0;
    }

    public void fillResult(ResultContext<Map<String, Object>> resultContext) {
        if (currentBufferSize >= fetchSize) {
            throw new IllegalStateException("Result buffer is full. Please call flushBuffer(ServiceContext) first.");
        }
        Map<String, Object> resultRow = resultContext.getResultObject();
        resultSetBuffer.add(resultRow);
        ++currentBufferSize;
    }

    public boolean isBufferFull() {
        return currentBufferSize == fetchSize;
    }

    public int flushBuffer(ServiceContext ctx) {
        int fetchedCount = executeInternal(ctx);
        resultSetBuffer = new ArrayList<>();
        return fetchedCount;
    }

    public void setFetchSize(int fetchSize) {
        if (fetchSize < 1) {
            fetchSize = 1;
        }
        this.fetchSize = fetchSize;
    }

    public void setCallbacks(List<AfterProcessCallback> callbacks) {
        if (callbacks == null) {
            return;
        }
        for (AfterProcessCallback callback : callbacks) {
            if (callback instanceof TransactionCleanupCallback
                    || callback instanceof SessionCleanupCallback) {
                continue;
            }
            this.callbacks.add(callback);
        }
    }
}
