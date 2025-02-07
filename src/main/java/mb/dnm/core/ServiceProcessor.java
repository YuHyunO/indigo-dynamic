package mb.dnm.core;

import mb.dnm.code.ProcessCode;
import mb.dnm.core.callback.AfterProcessCallback;
import mb.dnm.core.callback.SessionCleanupCallback;
import mb.dnm.core.callback.TransactionCleanupCallback;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.ErrorTrace;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;
import mb.dnm.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


/**
 * Service-Chaining, Error-Handling, Callback 을 수행하는 객체이다. 이 일련의 과정을 service processing 이라고 한다.<br>
 *
 * @see Service
 * @see StorageManager
 * @see InterfaceInfo
 * @see ErrorHandler
 * @see AfterProcessCallback
 * @see mb.dnm.service.AbstractService
 */
@Slf4j
public class ServiceProcessor {

    private static List<AfterProcessCallback> callbacks = new ArrayList<>();

    static {
        callbacks.add(new TransactionCleanupCallback()); //Set default
        callbacks.add(new SessionCleanupCallback()); //Set default
    }


    /**
     * Unfold services service context.
     *
     * @param ctx the ctx
     * @return the service context
     */
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
            log.info("[{}]Unfold the service strategy '{}'", txId, serviceId);
            for (Service service : services) {
                Class serviceClass = service.getClass();
                try {
                    if (ctx.isProcessOn()) {
                        ctx.addServiceTrace(serviceClass);
                        ++cnt1;
                        log.debug("[{}]Start the service '{}'({}/{})", txId, serviceClass, cnt1, serviceCount);
                        service.process(ctx);
                        if (!ctx.isProcessOn()) {
                            log.warn("[{}]Service chain is broken. An error may be exist.({}/{})\nService trace: {}", txId, cnt1, serviceCount, ctx.getServiceTraceMessage());
                            break;
                        }
                    }
                    if (ctx.getProcessStatus() != ProcessCode.ENFORCED_FAILURE) {
                        ctx.setProcessStatus(ProcessCode.SUCCESS);
                    } else {
                        ctx.setProcessStatus(ProcessCode.FAILURE);
                    }

                } catch (Throwable t0) {

                    ctx.addErrorTrace(serviceClass, t0);
                    String description = service.getDescription();
                    StringBuilder msg = new StringBuilder("An error occurred at the service '" + serviceClass + "'");
                    if (description != null) {
                        msg.append(" (").append(description).append(")");
                    }
                    ctx.setMsg(msg);

                    if (service.isIgnoreError()) {
                        log.warn("[{}]An error occurred at the service '{}' but ignored.({}/{}). Error:{}", txId, serviceClass, cnt1, serviceCount, MessageUtil.toString(t0));
                        continue;
                    }
                    ctx.setProcessStatus(ProcessCode.FAILURE);
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
                log.warn("[{}]No error handlers '{}' found for interface id '{}'", txId, errorHandlerId, interfaceId);
                return ctx;
            }

            int handlerCount = errorHandlers.size();
            int cnt2 = 0;
            for (ErrorHandler errorHandler : errorHandlers) {
                ++cnt2;
                if (!errorHandler.isTriggered(t1.getClass()))
                    continue;
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
            for (AfterProcessCallback callback : callbacks) {
                try {
                    log.debug("[{}]Processing callback: {}", txId, callback.getClass());
                    callback.afterProcess(ctx);
                } catch (Throwable t) {
                    log.error("[" + txId + "]Callback " + callback.getClass() + " process failed. Cause: ", t);
                }
            }
        }
        return ctx;
    }


    /**
     * {@code ServiceProcessor}에 callback 을 등록한다.
     *
     * @param callback the callback
     * @see mb.dnm.core.callback.RegistrableProcessCallback
     */
    public static void addCallback(AfterProcessCallback callback) {
        if (callbacks == null)
            throw new IllegalArgumentException("Callback must not be null");
        callbacks.add(callback);
    }
}
