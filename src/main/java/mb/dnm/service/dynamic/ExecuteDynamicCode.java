package mb.dnm.service.dynamic;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.code.ProcessCode;
import mb.dnm.core.dynamic.DynamicCodeInstance;
import mb.dnm.access.dynamic.DynamicCodeProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.AbstractService;
import mb.dnm.storage.InterfaceInfo;

import java.io.Serializable;

/**
 * DynamicCode 를 실행한다.
 * <br>
 * <br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;!-- {@link mb.dnm.storage.InterfaceInfo}의 dynamicCodeSequence에 등록된 순서대로 실행하는 경우--&gt;
 * &lt;bean class="mb.dnm.service.dynamic.ExecuteDynamicCode"/&gt;
 *
 * &lt;!-- {@link mb.dnm.storage.InterfaceInfo}의 dynamicCodeSequence에 등록된 특정 codeId를 지정하여 실행하는 경우--&gt;
 * &lt;bean class="mb.dnm.service.dynamic.ExecuteDynamicCode"&gt;
 *     &lt;property name="codeId"                value="<span style="color: black; background-color: #FAF3D4;">namespace.codeID</span>"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @see InterfaceInfo#getDynamicCodeSequence() InterfaceInfo#getDynamicCodeSequence()
 * @see ServiceContext#hasMoreDynamicCodes() ServiceContext#hasMoreDynamicCodes()
 * @see ServiceContext#nextDynamicCodeId() ServiceContext#nextDynamicCodeId()
 * @see ServiceContext#hasMoreErrorDynamicCodes() ServiceContext#hasMoreErrorDynamicCodes()
 * @see ServiceContext#nextErrorDynamicCodeId() ServiceContext#nextErrorDynamicCodeId()
 */
@Slf4j
public class ExecuteDynamicCode extends AbstractService implements Serializable {

    private static final long serialVersionUID = -5870630048628268142L;
    @Setter
    private String codeId;

    @Override
    public void process(ServiceContext ctx) throws Throwable {

        String codeId = null;
        if (this.codeId == null) {
            if (exceptionHandlingMode) {
                if (!ctx.hasMoreErrorDynamicCodes())
                    throw new InvalidServiceConfigurationException(this.getClass(), "No more errorDynamic code found in the dynamic code sequence queue");
                codeId = ctx.nextErrorDynamicCodeId();
            } else {
                if (!ctx.hasMoreDynamicCodes())
                    throw new InvalidServiceConfigurationException(this.getClass(), "No more dynamic code found in the dynamic code sequence queue");
                codeId = ctx.nextDynamicCodeId();
            }
        } else {
            codeId = this.codeId.replace("@{if_id}", ctx.getInterfaceId());
        }

        DynamicCodeInstance dnmInstance = DynamicCodeProvider.access().getDynamicCode(codeId);
        if (dnmInstance == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The dynamic code instance with id '" + codeId + "' is not exist in the DynamicCodeProvider.");
        }
        log.info("[{}]Executing dynamic code. id:'{}', instance: {}", ctx.getTxId(), codeId, dnmInstance.getDynamicCodeClassName());
        ProcessCode beforeCode = ctx.getProcessStatus();
        dnmInstance.execute(ctx);
        if (ctx.getProcessStatus() == ProcessCode.DYNAMIC_CODE_FAILURE) {
            log.warn("[{}]Dynamic code '{}' execution failure", ctx.getTxId(), codeId);
            ctx.setProcessStatus(beforeCode);
            Object errorObj = ctx.getContextParam(dnmInstance.getDynamicCodeClassName());
            if (errorObj instanceof Throwable) {
                throw (Throwable) errorObj;
            } else {
                log.warn("[{}]Couldn't get exception cause. Because the detected object '{}' is not Throwable", ctx.getTxId(), errorObj);
                throw new Exception("An error occurred while executing dynamic code: " + codeId);
            }

        }

    }

    /**
     * 실행할 codeId를 등록한다.
     *
     * @param codeId the code id
     */
    public void setCodeId(String codeId) {
        if (codeId == null)
            return;
        if (codeId.trim().isEmpty())
            return;
        this.codeId = codeId;
    }

}
