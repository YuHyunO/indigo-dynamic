package mb.dnm.service.dynamic;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.dynamic.DynamicCodeInstance;
import mb.dnm.access.dynamic.DynamicCodeProvider;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.AbstractService;
import mb.dnm.storage.InterfaceInfo;

/**
 * DynamicCode 를 실행한다.
 *
 * @see InterfaceInfo#getDynamicCodeSequence()
 *
 * @author Yuhyun O
 * @version 2024.09.30
 *
 * */
@Slf4j
public class ExecuteDynamicCode extends AbstractService {

    @Override
    public void process(ServiceContext ctx) throws Throwable {

        if (!ctx.hasMoreDynamicCodes())
            throw new InvalidServiceConfigurationException(this.getClass(), "No more dynamic code found in the dynamic code sequence queue");

        String codeId = ctx.nextDynamicCodeId();
        DynamicCodeInstance dnmInstance = DynamicCodeProvider.access().getDynamicCode(codeId);
        log.info("[{}]Executing dynamic code. id:'{}', instance: {}", ctx.getTxId(), codeId, dnmInstance.getDynamicCodeClassName());
        dnmInstance.execute(ctx);
    }

}
