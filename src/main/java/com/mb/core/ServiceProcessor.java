package com.mb.core;

import com.mb.storage.InterfaceInfo;
import com.mb.storage.StorageManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ServiceProcessor {

    public static TransactionContext unfoldServices(TransactionContext ctx) {
        if (ctx == null)
            throw new IllegalArgumentException("TransactionContext is null");

        InterfaceInfo info = ctx.getInfo();
        String interfaceId = info.getInterfaceId();
        String serviceId = info.getServiceId();
        String errorHandlerId = info.getErrorHandlerId();
        String txId = ctx.getTxId();

        List<Service> services = StorageManager.access().getServices(serviceId);
        if (services == null || services.isEmpty()) {
            log.warn("[{}]No services found for process id {}", interfaceId, serviceId);
            return ctx;
        }

        int serviceCount = services.size();
        int c1 = 0;
        for (Service service : services) {
            Class serviceClass = service.getClass();
            try {
                ctx.addServiceTrace(serviceClass);
                if (ctx.isProcessOn()) {
                    ++c1;
                    log.debug("[{}]Start the service '{}'({}/{})", interfaceId, serviceClass, c1, serviceCount);
                    service.process(ctx);
                } else {
                    log.warn("[{}]Service chain is broken. An error may be exist.({}/{})\nService trace: {}", interfaceId, c1, serviceCount, ctx.getServiceTraceMessage());
                    break;
                }
            } catch (Throwable t) {

            }

        }
        return ctx;
    }

}
