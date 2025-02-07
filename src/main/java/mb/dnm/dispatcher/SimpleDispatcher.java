package mb.dnm.dispatcher;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.ServiceProcessor;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple dispatcher.<br>
 * interfaceId를 파라미터로 받은 뒤 {@link InterfaceInfo}를 생성, Service-Process 를 실행한다.<br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 *     public void dispatch(String interfaceId) {
 *         InterfaceInfo info = StorageManager.access().getInterfaceInfo(interfaceId);
 *         if (info == null) {
 *             log.warn("There is no information about this interface id '{}'. Please check your configurations.", interfaceId);
 *             return;
 *         }
 *
 *         if (!info.isActivated()) {
 *             log.debug("This interface '{}' is not activated.", interfaceId);
 *             return;
 *         }
 *
 *         ServiceContext ctx = new ServiceContext(info);
 *         log.info("[{}]A new interface transaction was created", ctx.getTxId());
 *         ServiceProcessor.unfoldServices(ctx);
 *         log.info("[{}]The interface transaction was ended", ctx.getTxId());
 *     }
 * </pre>
 */
@Slf4j
public class SimpleDispatcher {

    /**
     * Dispatch.
     *
     * @param interfaceId the interface id
     */
    public void dispatch(String interfaceId) {
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
        log.info("[{}]A new interface transaction was created", ctx.getTxId());
        ServiceProcessor.unfoldServices(ctx);
        log.info("[{}]The interface transaction was ended", ctx.getTxId());
    }

}
