package mb.dnm.service.dynamic;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.dynamic.DynamicCodeInstance;
import mb.dnm.access.dynamic.DynamicCodeProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.AbstractService;
import mb.dnm.storage.InterfaceInfo;

/**
 * DynamicCode 를 실행한다.<br>
 * ServiceStorage에 등록된  <code>ExecuteDynamicCode</code> 서비스의 수와 <code>InterfaceInfo</code>의 <code>dynamicCodeSequence</code>에 등록된 dynamic code의 수는 동일해야한다.<br>
 * <code>ExecuteDynamicCode</code>가 실행될 때 마다 각 <code>dynamicCodeSequence</code>의 code 가 소진되기 때문이다.
 *
 * @see InterfaceInfo#getDynamicCodeSequence()
 * @see ServiceContext#hasMoreDynamicCodes()
 * @see ServiceContext#getCurrentDynamicCodeOrder()
 *
 * @author Yuhyun O
 * @version 2024.09.30
 *
 * @Throws InvalidServiceConfigurationException <code>InterfaceInfo</code>의 <code>dynamicCodeSequence</code>에 등록된 code가 더이상 존재하지 않는 경우
 * */
@Slf4j
public class ExecuteDynamicCode extends AbstractService {

    @Override
    public void process(ServiceContext ctx) throws Throwable {

        if (!ctx.hasMoreDynamicCodes())
            throw new InvalidServiceConfigurationException(this.getClass(), "No more dynamic code found in the dynamic code sequence queue");

        String codeId = ctx.nextDynamicCodeId();
        DynamicCodeInstance dnmInstance = DynamicCodeProvider.access().getDynamicCode(codeId);
        if (dnmInstance == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The dynamic code instance with id '" + codeId + "' is not exist in the DynamicCodeProvider.");
        }
        log.info("[{}]Executing dynamic code. id:'{}', instance: {}", ctx.getTxId(), codeId, dnmInstance.getDynamicCodeClassName());
        dnmInstance.execute(ctx);
    }

}
