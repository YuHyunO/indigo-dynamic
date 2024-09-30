package mb.dnm.service.general;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.code.ProcessCode;
import mb.dnm.core.ErrorHandler;
import mb.dnm.core.Service;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Input 파라미터로 Iterable 객체를 전달받아 <code>fetchSize</code> 만큼 반복하며 등록된 <code>List<Service> services</code>를 수행한다.<br>
 * <code>createNewContextEachLoop</code> 설정을 true로 한 경우에는 반복문이 수행되며 때마다 등록된 <code>List<Service> services</code>를 관통하는
 * <code>ServiceContext</code> 가 매번 새로 생성된다. 하지만 이렇게 반복마다 매번 생성되는 Context 객체는 하나의 반복문 안에서만 유효하다.<br><br>
 *
 * 이 서비스가 실행되기 전의 <code>ServiceContext</code> 객체는 사라지지 않는다.
 *
 * @author Yuhyun O
 * @version 2024.09.22
 * @Input 매번의 Iteration 에서 반복할 요소
 * @InputType <code>Iterable</code>
 * @Output <code>createNewContextEachLoop</code>가 true 인 경우 <code>List&lt;Service&gt; services</code>를 수행하며 output으로 지정된 값을 output으로 사용할 수 있다.
 * @OutputType Object
 * @Exceptions <code>InvalidServiceConfigurationException</code>: Input parameter의 타입이 Iterable 객체가 아닌 경우<br>
 *
 * */
@Slf4j
@Setter
public class IterationGroup extends ParameterAssignableService {
    /**
     * IterationGroup 에서 실행될 service strategies
     * */
    private List<Service> services;
    /**
     * 각각의 Iteration 에서 service strategies 를 실행하는 도중 에러발생 시 등록할 수 있는 error handlers
     * */
    private List<ErrorHandler> errorHandlers;
    /**
     * 각각의 Iteration 에서 service strategies 의 Input 매개변수로 전달될 요소명<br>
     * */
    private String iterationInputName;

    /**
     * 기본값: false<br>
     * <code>BreakIteration</code> 서비스를 통해 Break 가 멈출 때 까지 반복을 수행한다.<br>
     * 이 속성이 true 이지만 이 <code>IterationGroup</code> 에 등록된 서비스 중 <code>mb.dnm.service.general.BreakIteration</code> 이 존재하지 않는 경우 InvalidServiceConfigurationException 이 발생한다.<br>
     * 또 이 속성이 true로 지정되었을 때 <code>iterationInputName</code> 속성 지정을 통한 input 파라미터 전달은 효력이 없다.
     * */
    private boolean iterateUntilBreak = false;
    /**
     * 기본값: 1<br>
     * 각각의 Iteration의 반복되는 요소가 service strategies 의 Input 매개변수로 전달될 때 몇 개만큼 전달될 지를 결정한다.<br>
     * */
    private int fetchSize = 1;
    /**
     * 기본값: false<br>
     * 각각의 Iteration 에서 매번 새로운 ServiceContext를 생성할 지에 대한 설정
     * */
    private boolean createNewContextEachLoop = false;

    private boolean initialCheck = false;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null && !iterateUntilBreak) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The IterationGroup requires a parameter to execute repeated elements.");
        }

        if (!initialCheck && iterateUntilBreak) {
            for (Service service : services) {
                if (service.getClass() == BreakIteration.class) {
                    initialCheck = true;
                    break;
                }
            }
            if (!initialCheck)
                throw new InvalidServiceConfigurationException(this.getClass(), "The IterationGroup must requires a BreakIteration class when the property 'iterateUntilBreak' is true");
        }

        if (iterationInputName == null || iterationInputName.isEmpty()) {
            if (!iterateUntilBreak) {
                throw new InvalidServiceConfigurationException(this.getClass(), "The property 'iterationInputName' is null. The IterationGroup requires the \"iteration input name\" which to be passed into inner loop service's input parameter.");
            }
        }

        InterfaceInfo info = ctx.getInfo();
        String interfaceId = info.getInterfaceId();
        String txId = ctx.getTxId();

        //반복문 수행 시 query sequence가 매번 초기화 되어야 하므로 반복문을 수행하기 전의 query order 를 미리 저장한다.
        int currentQueryOrder = ctx.getCurrentQueryOrder();
        int currentErrorQueryOrder = ctx.getCurrentErrorQueryOrder();
        int currentDynamicCodeOrder = ctx.getCurrentDynamicCodeOrder();


        // 우선 iterateUntilBreak 조건에 따라 로직을 나눔 (코드정리 작업은 나중에 수행)
        if (iterateUntilBreak) {
            int serviceCount = services.size();

            int iterCnt = 0;
            boolean breaked = false;
            while (true) {
                ServiceContext innerCtx = null;
                String innerTxId = null;
                if (createNewContextEachLoop) {
                    //createNewContextEachLoop=true 인 경우 각 반복문에서 Inner Processing service chaining 을 할 때 전달될 ServiceContext 객체를 새로 생성함
                    innerCtx = new ServiceContext(info);
                    innerTxId = innerCtx.getTxId();
                    innerCtx.addContextParam("$iter_break", false);
                    log.debug("[{}]Iteration-Group: New service context is created.", innerTxId);
                } else {
                    innerCtx = ctx;
                    innerTxId = txId;
                    innerCtx.addContextParam("$iter_break", false);
                }

                //매번의 반복문에서 같은 QueryOrder 를 지정해준다.
                innerCtx.setCurrentQueryOrder(currentQueryOrder);
                innerCtx.setCurrentErrorQueryOrder(currentErrorQueryOrder);
                innerCtx.setCurrentDynamicCodeOrder(currentDynamicCodeOrder);

                int cnt1 = 0;
                try {
                    //Inner Processing service chaining 시작
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

                            if ((boolean) innerCtx.getContextParam("$iter_break")) {
                                log.debug("[{}]Iteration-Group: Breaking this iteration group. Total iteration count: {}", txId, iterCnt);
                                breaked = true;
                                break;
                            }
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
                } finally {
                    if (breaked) {
                        break;
                    }
                    ++iterCnt;
                }
            }
            ctx.deleteContextParam("$iter_break");
            log.debug("[{}]Iteration-Group: Total iteration count: {}", txId, iterCnt);

        } else {
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

            /*fetchSize 가 1보다 큰 경우 Inner Processing service chaining 의 Input 파라미터의 데이터 타입으로 List 가 전달됨.
            이때의 List 의 size는 fetchSize 보다 작거나 같다.
             */
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
                    //createNewContextEachLoop=true 인 경우 각 반복문에서 Inner Processing service chaining 을 할 때 전달될 ServiceContext 객체를 새로 생성함
                    innerCtx = new ServiceContext(info);
                    innerTxId = innerCtx.getTxId();
                    log.debug("[{}]Iteration-Group: New service context is created.", innerTxId);
                } else {
                    innerCtx = ctx;
                    innerTxId = txId;
                }

                //매번의 반복문에서 같은 QueryOrder 를 지정해준다.
                innerCtx.setCurrentQueryOrder(currentQueryOrder);
                innerCtx.setCurrentErrorQueryOrder(currentErrorQueryOrder);
                innerCtx.setCurrentDynamicCodeOrder(currentDynamicCodeOrder);

                /*Inner Processing services 에서는 이 서비스에 등록된 iterationInputName 으로 input 을 파라미터로 받을 수 있다.
                  지금 이 코드는 ParameterAssignableService#setOutputValue(ServiceContext ctx, Object outputValue); 와 같은 효과를 가진다.
                 */
                innerCtx.addContextParam(this.iterationInputName, val);


                log.debug("[{}]Iteration-Group: Fetching {} element from the input parameter '{}' ...", innerTxId, fetched, getInput());
                int cnt1 = 0;
                try {
                    //Inner Processing service chaining 시작
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

            //반복문 수행이 끝나면 ServiceContext에 원래의 QueryOrder를 다시 지정해줘야 한다.
            ctx.setCurrentQueryOrder(currentQueryOrder);
            ctx.setCurrentErrorQueryOrder(currentErrorQueryOrder);
            ctx.setCurrentDynamicCodeOrder(currentDynamicCodeOrder);

            log.debug("[{}]Iteration-Group: Total iteration count: {}", txId, iterCnt);
        }

        if (createNewContextEachLoop) {
            if (getOutput() != null) {
                setOutputValue(ctx, ctx.getContextParam(getOutput()));
            }
        }
    }

    public void setIterationInputName(String iterationInputName) {
        if (this.iterateUntilBreak)
            throw new InvalidServiceConfigurationException(this.getClass(), "The property 'iterateUntilBreak' can't be true when 'iterationInputName' is exist.");
        this.iterationInputName = iterationInputName;
    }

    public void setIterateUntilBreak(boolean iterateUntilBreak) {
        if (iterationInputName != null && iterateUntilBreak)
            throw new InvalidServiceConfigurationException(this.getClass(), "The property 'iterationInputName' can't be exist when 'iterateUntilBreak' is true.");
        this.iterateUntilBreak = iterateUntilBreak;
    }

    public void setFetchSize(int fetchSize) {
        if (fetchSize <= 0) {
            throw new InvalidServiceConfigurationException(this.getClass(), "Iteration fetch size must be greater than 0.");
        }
        this.fetchSize = fetchSize;
    }

    private void SetInitialCheck(boolean initialCheck) {
        this.initialCheck = initialCheck;
    }
}
