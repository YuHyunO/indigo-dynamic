package mb.dnm.core;

import mb.dnm.code.ProcessCode;
import mb.dnm.core.callback.AfterProcessCallback;
import mb.dnm.core.callback.SessionCleanupCallback;
import mb.dnm.core.callback.TransactionCleanupCallback;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;
import mb.dnm.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


/**
 * <i><b>Service chaining</b></i>  을 통해 일련의 <code>mb.dnm.core.Service</code> 를 수행하는 핵심 클래스이다.<br><br>
 * 실행되어야 하는 서비스는 <code>mb.dnm.storage.StorageManager</code>에 <i>키(<code>String serviceId</code>):값(<code>List&lt;Service&gt; serviceList</code>)</i>형태로 등록이 되어있어야 한다.
 * 특정 이벤트가 발생할 때 마다 <code>ServiceProcessor</code>의 static 메소드인 <code>unfoldServices(ServiceContext)</code>를 호출하도록 어플리케이션을 만들면
 * 각각의 인터페이스 정보를 담고있는 클래스인 <code>mb.dnm.storage.InterfaceInfo</code>클래스의 <code>getServiceId()</code> 메소드를 통해 가져온 값과 매핑되는 <code>List&lt;Service&gt; serviceList</code>를
 * <code>StorageManager</code> 에서 가져와 <i>Service chaining</i>을 수행한다.<br><br>
 *
 * <i><b>Service chaining</b></i> 이 진행되는 도중 Exception이 발생하면 <i><b>Service chaining</b></i> 은 종료된다.<br>
 * 이 경우 Exception 발생하는 경우에 대한 처리를 지원하기 위해 <code>mb.dnm.core.ErrorHandler</code> 인터페이스를 제공한다.<br>
 * <code>ErrorHandler</code> 또한 <code>StorageManager</code>에 <i>키(<code>String errorHandlerId</code>):값(<code>List&lt;ErrorHandler&gt; errorHandlerList</code>)</i>형태로 등록이 되며
 * 에러처리 프로세스는 <code>InterfaceInfo</code> 클래스의 <code>getErrorHandlerId()</code> 메소드를 통해 가져온 <code>errorHandlerId</code>와 일치하는
 * <code>List&lt;ErrorHandler&gt; errorHandlerList</code> 를 가져오는 것이므로 <code>StorageManager</code> 에는 동일한 <code>errorHandlerId</code> 를 가진 ErrorHandler가 등록되어야 한다.<br><br>
 *
 * <i><b>Error handling</b></i> 프로세스 또한 여러 ErrorHandler를 Chaining 하는 방식으로 수행되지만 <i><b>Service chaining</b></i> 과정과 다른점은
 * <i><b>Error handling</b></i> 도중에 Exception이 발생하더라도 Chaining이 도중에 중단되지 않는다는 점이다.
 * 따라서 특정한 <code>errorHandlerId</code> 를 가진 <code>List&lt;ErrorHandler&gt; errorHandlerList</code>의 모든 ErrorHandler는 에러 발생여부와 무관하게 모두 실행된다.<br>
 *
 * <i><b>Service chaining</b></i> 도중 Exception이 발생하지 않더라도 특별한 이유로 <i><b>Service chaining</b></i>을 멈추도록 하고 싶은 경우에는
 * <code>mb.dnm.core.Service</code> 인터페이스를 구현할 때  <code>process(ServiceContext)</code> 메소드 내부에 Chaining을 멈춰야 하는 조건이 포함된 로직에서
 * <code>mb.dnm.core.context.ServiceContext</code> 클래스의 <code>setProcessOn(boolean)</code> 메소드를 호출하여 <code>ServiceContext#processOn</code> 속성을 <code>false</code> 로 변경해주면 된다.<br>
 *
 * 또 <i><b>Service chaining</b></i> 도중 특정한 <code>Service</code> 에서 Exception이 발생하더라도 Exception을 무시하고 Chaining 프로세스를 계속 진행하고 싶은 경우에는 해당 조건을 <code>try catch</code> 문으로
 * 처리해주는 방법도 있지만 <code>Service</code> 인터페이스의 <code>setIgnoreError(boolean)</code>와 <code>isIgnoreError()</code> 메소드를 구현하여 Chaining 이 멈추지 않도록 처리할 수도 있다.
 * 추상클래스인 <code>mb.dnm.service.AbstractService</code> 는 <code>Service</code> 인터페이스의 <code>process(ServiceContext)</code> 메소드를 제외한 위 메소드를 기본적으로 구현하고 있기 때문에
 * <code>Service</code> 인터페이스를 직접적으로 구현하기보다 <code>mb.dnm.service.AbstractService</code> 추상클래스를 상속받아 <code>Service</code>를 구현하는 것을 권장한다.<br><br>
 *
 * <code>ServiceProcessor</code> 의 <code>unfoldServices(ServiceContext)</code> 메소드 내에서 마지막으로 수행되는 프로세스는 Callback이다.
 * <code>mb.dnm.core.callback.AfterProcessCallback</code> 인터페이스의 구현체를 <code>ServiceProcessor.addCallback(AfterProcessCallback)</code> 메소드의 파라미터로 지정해 사용함으로써 
 * 특정 이벤트를 통해 발생한 하나의 프로세스의 종료 시에 수행될 수 있는 Callback 프로세스들을 등록할 수 있다. Callback 프로세스는 등록된 수 만큼 실행되며
 * ErrorHandling 프로세스와 마찬가지로 Callback 수행 도중 Exception이 발생하더라도 나머지 Callback은 마저 실행된다.
 * <code>ServiceProcessor</code>에는 기본적으로, DB관련 작업 후 처리되지 않은 트랜잭션을 에러발생 여부에 따라 commit 또는 rollback 처리하는 구현체인 <code>mb.dnm.core.callback.TransactionCleanupCallback</code>과
 * FTP나 SFTP 서버 등과의 닫히지 않은 세션을 close 해주는 <code>mb.dnm.core.callback.SessionCleanupCallback</code> 가 등록된다.
 *
 * @see Service
 * @see StorageManager
 * @see InterfaceInfo
 * @see ErrorHandler
 * @see AfterProcessCallback
 * @see mb.dnm.service.AbstractService
 *
 * @author Yuhyun O
 * @version 2024.09.02
 * */
@Slf4j
public class ServiceProcessor {

    private static List<AfterProcessCallback> callbacks = new ArrayList<>();

    static {
        callbacks.add(new TransactionCleanupCallback()); //Set default
        callbacks.add(new SessionCleanupCallback()); //Set default
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
            log.info("[{}]Unfold the service strategy '{}'", txId, serviceId);
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
                log.warn("[{}]No error handlers '{}' found for interface id '{}'", txId, errorHandlerId, interfaceId);
                return ctx;
            }

            int handlerCount = errorHandlers.size();
            int cnt2 = 0;
            for (ErrorHandler errorHandler : errorHandlers) {
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
            for (AfterProcessCallback listener : callbacks) {
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


    public static void addCallback(AfterProcessCallback callback) {
        if (callbacks == null)
            throw new IllegalArgumentException("Callback must not be null");
        callbacks.add(callback);
    }
}
