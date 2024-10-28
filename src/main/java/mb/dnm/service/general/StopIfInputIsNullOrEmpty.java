package mb.dnm.service.general;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.SizeCheckable;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

import java.util.Collection;
import java.util.Map;

/**
 *
 * Input이 Null이거나 Empty 인 경우 프로세스를 중단한다
 *
 * @author Yuhyun O
 * @version 2024.09.26
 *
 * @Input Null 또는 Empty 를 체크할 input의 이름
 * @InputType <code>Map</code> or <code>Collection</code> or <code>SizeCheckable</code>
 * @Output 프로세스를 중단하고 나서 Callback 프로세스 등에서 사용할 목적으로 중단에 대한 정보를 담고 있는 변수가 필요한 경우 output 할 변수의 이름
 * @OutputValue output이 지정된 경우 output 할 변수의 값
 * @OutputType <code>Object</code>
 *
 * */
@Slf4j
@Setter
public class StopIfInputIsNullOrEmpty extends ParameterAssignableService {
    private Object outputValue;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        Object inputVal = getInputValue(ctx);
        
        boolean stop = false;
        boolean nill = false;
        if (inputVal == null) {
            nill = true;
            stop = true;
            
        } else if (inputVal instanceof Collection) {
            if (((Collection) inputVal).isEmpty()) {
                stop = true;
            }

        } else if (inputVal instanceof Map) {
            if (((Map) inputVal).isEmpty()) {
                stop = true;
            }

        } else if (inputVal.getClass().isArray()) {
            int len = 0;
            if (inputVal instanceof byte[]) {
                len = ((byte[]) inputVal).length;
            } else if (inputVal instanceof int[]) {
                len = ((int[]) inputVal).length;
            } else if (inputVal instanceof long[]) {
                len = ((long[]) inputVal).length;
            } else if (inputVal instanceof double[]) {
                len = ((double[]) inputVal).length;
            } else if (inputVal instanceof float[]) {
                len = ((float[]) inputVal).length;
            } else if (inputVal instanceof boolean[]) {
                len = ((boolean[]) inputVal).length;
            } else if (inputVal instanceof char[]) {
                len = ((char[]) inputVal).length;
            } else if (inputVal instanceof Object[]) {
                len = ((Object[]) inputVal).length;
            }

            if (len == 0) {
                stop = true;
            }

        } else if (inputVal instanceof SizeCheckable) {
            if (((SizeCheckable) inputVal).isEmpty()) {
                ctx.setProcessOn(false);
                stop = true;
            }
        } else if (Number.class.isAssignableFrom(inputVal.getClass())) {
            if (((Number)inputVal).intValue() == 0) {
                ctx.setProcessOn(false);
                stop = true;
            }
        }

        if (stop) {
            String txId = ctx.getTxId();
            if (nill) {
                log.info("[{}]Input value is null. The service process will be stop", ctx.getTxId());
            } else {
                log.info("[{}]Input value is empty. The service process will be stop", ctx.getTxId());
            }
            ctx.setProcessOn(false);
            if (output != null) {
                log.debug("[{}]Setting output '{}' with value '{}'...", txId, output, outputValue);
                ctx.addContextParam(output, outputValue);
            }
        }

    }
}
