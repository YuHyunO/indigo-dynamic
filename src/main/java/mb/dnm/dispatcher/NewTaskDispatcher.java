package mb.dnm.dispatcher;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.ServiceProcessor;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 인터페이스 이벤트를 수신한 Thread 와 다른 Thread 에서 Service-process 를 실행한다.
 */
@Slf4j
public class NewTaskDispatcher {
    private static ExecutorService executorService = null;

    /**
     * Dispatch.
     *
     * @param interfaceId the interface id
     */
    public static void dispatch(final String interfaceId) {
        dispatchWithParam(interfaceId, new HashMap<String, Object>());
    }

    /**
     * Dispatch with param.
     *
     * @param interfaceId   the interface id
     * @param contextParams the context params
     */
    public static void dispatchWithParam(final String interfaceId, final Map<String, Object> contextParams) {

        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                InterfaceInfo info = StorageManager.access().getInterfaceInfo(interfaceId);
                if (info == null) {
                    log.warn("There is no information about this interface id '{}'. Please check your configurations.", interfaceId);
                    return;
                }

                if (!info.isActivated()) {
                    log.debug("This interface '{}' is not activated.", interfaceId);
                    return;
                }

                ServiceContext ctx = new ServiceContext(info);
                String txId = ctx.getTxId();
                log.info("[{}]A new interface transaction was created", txId);

                if (contextParams != null) {
                    for (Map.Entry<String, Object> entry : contextParams.entrySet()) {
                        String key = entry.getKey();
                        ctx.addContextParam(key, entry.getValue());
                        log.info("[{}]Added context parameter named '{}'", txId, key);
                    }
                }

                ServiceProcessor.unfoldServices(ctx);
                log.info("[{}]The interface transaction was ended", txId);
            }
        });

    }


}
