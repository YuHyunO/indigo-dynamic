package mb.dnm.core;

import mb.dnm.code.ProcessCode;
import mb.dnm.core.callback.AfterProcessListener;
import mb.dnm.core.callback.TransactionCleanupListener;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;
import mb.dnm.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ServiceProcessor {

    private static List<AfterProcessListener> callbacks = new ArrayList<>();

    static {
        callbacks.add(new TransactionCleanupListener()); //Set default CallbackListener
    }


    public static ServiceContext unfoldServices(ServiceContext ctx) {
        if (ctx == null)
            throw new IllegalArgumentException("TransactionContext is null");

        InterfaceInfo info = ctx.getInfo();
        String interfaceId = info.getInterfaceId();
        String serviceId = info.getServiceId();
        String errorHandlerId = info.getErrorHandlerId();
        String txId = ctx.getTxId();

        List<Service> services = StorageManager.access().getServices(serviceId);
        if (services == null || services.isEmpty()) {
            log.warn("[{}]No services '{}' found for interface id '{}'", txId, serviceId, interfaceId);
            return ctx;
        }

        int serviceCount = services.size();
        int cnt1 = 0;
        try {

            //Processing service chaining
            ctx.setProcessStatus(ProcessCode.IN_PROCESS);
            for (Service service : services) {
                Class serviceClass = service.getClass();
                ctx.addServiceTrace(serviceClass);
                try {
                    if (ctx.isProcessOn()) {
                        ++cnt1;
                        log.debug("[{}]Start the service '{}'({}/{})", txId, serviceClass, cnt1, serviceCount);
                        service.process(ctx);
                    } else {
                        log.warn("[{}]Service chain is broken. An error may be exist.({}/{})\nService trace: {}", txId, cnt1, serviceCount, ctx.getServiceTraceMessage());
                        break;
                    }
                    ctx.setProcessStatus(ProcessCode.SUCCESS);
                } catch (Throwable t0) {
                    ctx.setProcessStatus(ProcessCode.FAILURE);
                    ctx.addErrorTrace(serviceClass, t0);
                    if (service.isIgnoreError()) {
                        log.warn("[{}]An error occurred at the service '{}' but ignored.({}/{}). Error:{}", txId, serviceClass, cnt1, serviceCount, MessageUtil.toString(t0));
                        continue;
                    }
                    log.error("[" + txId + "]Service chain is broken. An error occurred at the service '" + serviceClass + "'", t0);
                    throw t0;
                } finally {
                    ctx.stampEndTime();
                    log.debug("[{}]End the service '{}'", txId, serviceClass);
                }
            }
        } catch (Throwable t1) {
            //Processing error handling
            List<ErrorHandler> errorHandlers = StorageManager.access().getErrorHandlersById(errorHandlerId);
            if (errorHandlers == null || errorHandlers.isEmpty()) {
                log.warn("[{}]No error handlers '{}' found for interface id '{}'", txId, serviceId, interfaceId);
                return ctx;
            }

            int handlerCount = errorHandlers.size();
            int cnt2 = 0;
            for (ErrorHandler errorHandler : errorHandlers) {
                Class errorHandlerClass = errorHandler.getClass();
                try {
                    log.warn("[{}]Start the error handler '{}'({}/{})", txId, errorHandlerClass, cnt2, handlerCount);
                    errorHandler.handleError(ctx);
                } catch (Throwable t2) {
                    log.error("[" + txId + "]An error occurred when handling error.(" + cnt2 + "/" + handlerCount
                            + ") Error handler id:'" + errorHandlerId + "', Error handler class: '" + errorHandlerClass + "'"
                            + "Continue error handling process.", t2);
                } finally {
                    //* Set the end time at each end of error handler
                    ctx.stampEndTime();
                    log.debug("[{}]End the error handler '{}'", txId, errorHandlerClass);
                }
            }

        } finally {
            //Processing callbacks
            for (AfterProcessListener listener : callbacks) {
                try {
                    log.debug("[{}]Processing callback: {}", txId, listener.getClass());
                    listener.afterProcess(ctx);
                } catch (Throwable t) {
                    log.error("[" + txId + "]Callback " + listener.getClass() + " process failed. Cause: ", t);
                }
            }
        }
        return ctx;
    }


    public void addCallbackListener(AfterProcessListener listener) {
        if (callbacks == null)
            throw new IllegalArgumentException("Callback listener must not be null");
        callbacks.add(listener);
    }
}
