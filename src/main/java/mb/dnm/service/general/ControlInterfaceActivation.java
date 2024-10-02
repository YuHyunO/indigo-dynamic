package mb.dnm.service.general;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;

import java.util.Map;


/**
 * key로 IF_ID_KEYWORD 와 COMMAND_KEYWORD 가 포함된 Map 형태의 command 를 받아 인터페이스를 활성화 또는 비활성화 한다.
 * command 의 결과메시지를 output 한다.
 *
 * @author Yuhyun O
 * @version 2024.10.03
 *
 * */
@Slf4j
public class ControlInterfaceActivation extends ParameterAssignableService {

    private final String IF_ID_KEYWORD = "$$IF_ID";
    private final String COMMAND_KEYWORD = "$$COMMAND";

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "ControlInterfaceActivation service must have the input parameter");
        }

        Object inputVal = getInputValue(ctx);
        if (inputVal == null) {
            log.warn("[{}]No command parameters are found", ctx.getTxId());
            return;
        }

        if (getOutput() == null) {
            setOutput("$$COMMAND_RESULT");
        }

        try {
            if (inputVal instanceof Map) {
                Map<String, Object> commandMap = (Map<String, Object>) inputVal;
                String ifId = String.valueOf(commandMap.get(IF_ID_KEYWORD));
                String command = String.valueOf(commandMap.get(COMMAND_KEYWORD)).trim();

                if (ifId.equals("null")) {
                    setOutputValue(ctx, "The interfaceId is null");
                    return;
                }

                InterfaceInfo info = StorageManager.access().getInterfaceInfo(ifId);
                if (info == null) {
                    setOutputValue(ctx, "There is no interface with id '" + ifId + "'");
                    log.info("[{}]No interface with id '{}' found", ctx.getTxId(), ifId);
                    return;
                }

                if (command.equals("ACTIVE")) {
                    if (info.isActivated()) {
                        setOutputValue(ctx, "The interface '" + ifId + "' is already activated");
                        return;
                    }
                    StorageManager.access().activateInterface(ifId);
                    log.info("[{}]The interface '{}' is activated", ctx.getTxId(), info.getInterfaceId());
                } else if (command.equals("INACTIVE")) {
                    if (!info.isActivated()) {
                        setOutputValue(ctx, "The interface '" + ifId + "' is already inactivated");
                        return;
                    }
                    StorageManager.access().inactivateInterface(ifId);
                    log.info("[{}]The interface '{}' is inactivated", ctx.getTxId(), info.getInterfaceId());
                } else {
                    setOutputValue(ctx, "The command '" + command + "' is unknown");
                    log.info("[{}]The command '" + command + "' is unknown", ctx.getTxId());
                    return;
                }



            } else {
                log.warn("[{}]Invalid command parameter type. Has no effect", ctx.getTxId());
                return;
            }
        } catch (ClassCastException e) {
            log.warn("[{}]Invalid command parameter type", ctx.getTxId());
            return;
        }

    }

}
