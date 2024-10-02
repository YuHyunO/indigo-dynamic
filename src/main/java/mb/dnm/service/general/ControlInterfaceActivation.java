package mb.dnm.service.general;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;

import java.util.HashMap;
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
    @Setter
    private String key = "5uIW8zL0AmVQN9xdP7GoWPlsl7115asdqwDWEHd3RsT2fDA1yNcG4AyBbKdJMiC";
    private final String IF_ID_KEYWORD = "$$IF_ID";
    private final String COMMAND_KEYWORD = "$$COMMAND";
    private final String KEY = "$$KEY";

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "ControlInterfaceActivation service must have the input parameter");
        }

        Map<String, Object> response = new HashMap<>();

        Object inputVal = getInputValue(ctx);
        if (inputVal == null) {
            log.warn("[{}]No command parameters are found", ctx.getTxId());
            response.put("message", "No command parameters are found");
            setOutputValue(ctx, response);
            return;
        }

        if (getOutput() == null) {
            setOutput("$$COMMAND_RESULT");
        }

        try {
            if (inputVal instanceof Map) {
                Map<String, Object> commandMap = (Map<String, Object>) inputVal;
                String ifId = String.valueOf(commandMap.get(IF_ID_KEYWORD)).trim();
                String command = String.valueOf(commandMap.get(COMMAND_KEYWORD)).trim();

                String commandKey = String.valueOf(commandMap.get(KEY)).trim();
                if (!key.equals(commandKey)) {
                    response.put("message", "Not permitted");
                    setOutputValue(ctx, response);
                    return;
                }

                if (ifId.equals("null")) {
                    response.put("message", "The interfaceId is null");
                    setOutputValue(ctx, response);
                    return;
                }

                InterfaceInfo info = StorageManager.access().getInterfaceInfo(ifId);
                if (info == null) {
                    response.put("message", "There is no interface with id '" + ifId + "'");
                    setOutputValue(ctx, response);
                    log.info("[{}]No interface with id '{}' found", ctx.getTxId(), ifId);
                    return;
                }

                if (ifId.equals(ctx.getInterfaceId())) {
                    response.put("message", "There is no interface with id '" + ifId + "'");
                    setOutputValue(ctx, response);
                    log.info("[{}]Prohibited command detected. requested interface id '{}' is same with this context", ctx.getTxId(), ifId);
                    return;
                }

                if (command.equals("ACTIVE")) {
                    if (info.isActivated()) {
                        response.put("message", "The interface '" + ifId + "' is already activated");
                        setOutputValue(ctx, response);
                        return;
                    }
                    StorageManager.access().activateInterface(ifId);
                    response.put("message", "The interface '" + ifId + "' is activated");
                    setOutputValue(ctx, response);
                    log.info("[{}]The interface '{}' is activated", ctx.getTxId(), info.getInterfaceId());
                } else if (command.equals("INACTIVE")) {
                    if (!info.isActivated()) {
                        response.put("message", "The interface '" + ifId + "' is already inactivated");
                        setOutputValue(ctx, response);
                        return;
                    }
                    StorageManager.access().inactivateInterface(ifId);
                    response.put("message", "The interface '" + ifId + "' is inactivated");
                    setOutputValue(ctx, response);
                    log.info("[{}]The interface '{}' is inactivated", ctx.getTxId(), info.getInterfaceId());
                } else {
                    response.put("message", "The command '" + command + "' is unknown");
                    setOutputValue(ctx, response);
                    log.info("[{}]The command '" + command + "' is unknown", ctx.getTxId());
                }

            } else {
                log.warn("[{}]Invalid command parameter type. Has no effect", ctx.getTxId());
                response.put("message", "parameters types are invalid");
                setOutputValue(ctx, response);

            }
        } catch (ClassCastException e) {
            log.warn("[{}]Invalid command parameter type", ctx.getTxId());
            response.put("message", "parameters types are invalid");
            setOutputValue(ctx, response);
        }

    }

}
