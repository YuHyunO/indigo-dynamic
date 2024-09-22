package mb.dnm.service.general;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.code.ProcessCode;
import mb.dnm.core.ErrorHandler;
import mb.dnm.core.Service;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Setter
public class IterationGroup extends ParameterAssignableService {
    private List<Service> services;
    private List<ErrorHandler> errorHandlers;
    private String iterationInputName;
    private int fetchSize = 1;
    private boolean createNewContextEachLoop = false;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The IterationGroup requires a parameter to execute repeated elements.");
        }

        InterfaceInfo info = ctx.getInfo();
        String interfaceId = info.getInterfaceId();
        String txId = ctx.getTxId();

        Object inputVal = getInputValue(ctx);
        if (inputVal == null) {
            log.debug("[{}]The value of input '{}' is not found. No file paths to read found in context data.", txId, getInput());
            return;
        }

        if (services == null || services.isEmpty()) {
            log.warn("[{}]No services are found for this iterationGroup", txId);
        }
        int serviceCount = services.size();

        if (!Iterable.class.isAssignableFrom(inputVal.getClass())) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The class '" + inputVal.getClass() + "' is not Iterable");
        }

        Iterator iterator = ((Iterable) inputVal).iterator();
        int iterCnt = 0;
        while (iterator.hasNext()) {
            ++iterCnt;
            Object val = null;
            List<Object> valList = new ArrayList<>();
            int fetched = 0;
            if (fetchSize > 1) {
                while (iterator.hasNext()) {
                    ++fetched;
                    Object inval = iterator.next();
                    valList.add(inval);
                    if (fetchSize == fetched) {
                        break;
                    }
                }
                val = valList;
            } else {
                ++fetched;
                val = iterator.next();
            }

            ServiceContext innerCtx = null;
            String innerTxId = null;
            if (createNewContextEachLoop) {
                innerCtx = new ServiceContext(info);
                innerTxId = innerCtx.getTxId();
                log.debug("[{}]Iteration-Group: New service context is created.", innerTxId);
            } else {
                innerCtx = ctx;
                innerTxId = txId;
            }
            innerCtx.addContextParam(this.iterationInputName, val);


            log.debug("[{}]Iteration-Group: Fetching {} element from the input parameter '{}' ...", innerTxId, fetched, getInput());
            int cnt1 = 0;
            try {
                //Processing service chaining
                innerCtx.setProcessStatus(ProcessCode.IN_PROCESS);
                log.info("[{}]Iteration-Group: Unfold the services", innerTxId);
                for (Service service : services) {
                    Class serviceClass = service.getClass();
                    innerCtx.addServiceTrace(serviceClass);
                    try {
                        if (innerCtx.isProcessOn()) {
                            ++cnt1;
                            log.debug("[{}]Iteration-Group: Start the service '{}'({}/{})", innerTxId, serviceClass, cnt1, serviceCount);
                            service.process(innerCtx);
                        } else {
                            log.warn("[{}]Iteration-Group: Service chain is broken. An error may be exist.({}/{})\nService trace: {}", innerTxId, cnt1, serviceCount, innerCtx.getServiceTraceMessage());
                            break;
                        }
                        innerCtx.setProcessStatus(ProcessCode.SUCCESS);
                    } catch (Throwable t0) {
                        innerCtx.setProcessStatus(ProcessCode.FAILURE);
                        innerCtx.addErrorTrace(serviceClass, t0);
                        if (service.isIgnoreError()) {
                            log.warn("[{}]Iteration-Group: An error occurred at the service '{}' but ignored.({}/{}). Error:{}", innerTxId, serviceClass, cnt1, serviceCount, MessageUtil.toString(t0));
                            continue;
                        }
                        log.error("[" + innerTxId + "]Iteration-Group: Service chain is broken. An error occurred at the service '" + serviceClass + "'", t0);
                        throw t0;
                    } finally {
                        innerCtx.stampEndTime();
                        log.debug("[{}]Iteration-Group: End the service '{}'", innerTxId, serviceClass);
                    }
                }
            } catch (Throwable t1) {
                //Processing error handling
                if (errorHandlers == null || errorHandlers.isEmpty()) {
                    log.warn("[{}]Iteration-Group: No error handlers are found for this iteration group", innerTxId);
                }

                int handlerCount = errorHandlers.size();
                int cnt2 = 0;
                for (ErrorHandler errorHandler : errorHandlers) {
                    if (!errorHandler.isTriggered(t1.getClass()))
                        continue;
                    Class errorHandlerClass = errorHandler.getClass();
                    try {
                        log.warn("[{}]Start the error handler '{}'({}/{})", innerTxId, errorHandlerClass, cnt2, handlerCount);
                        errorHandler.handleError(innerCtx);
                    } catch (Throwable t2) {
                        log.error("[" + innerTxId + "]Iteration-Group: An error occurred when handling error.(" + cnt2 + "/" + handlerCount
                                + "), Error handler class: '" + errorHandlerClass + "'"
                                + "Continue error handling process.", t2);
                    } finally {
                        //* Set the end time at each end of error handler
                        innerCtx.stampEndTime();
                        log.debug("[{}]End the error handler '{}'", innerTxId, errorHandlerClass);
                    }
                }
            }
        }
        log.debug("[{}]Iteration-Group: Total iteration count: {}", txId, iterCnt);

    }

    public void setFetchSize(int fetchSize) {
        if (fetchSize <= 0) {
            throw new InvalidServiceConfigurationException(this.getClass(), "Iteration fetch size must be greater than 0.");
        }
        this.fetchSize = fetchSize;
    }

}
