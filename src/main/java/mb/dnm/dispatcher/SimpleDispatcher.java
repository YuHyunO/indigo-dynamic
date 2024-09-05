package mb.dnm.dispatcher;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.ServiceProcessor;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;

@Slf4j
public class SimpleDispatcher {
    
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
