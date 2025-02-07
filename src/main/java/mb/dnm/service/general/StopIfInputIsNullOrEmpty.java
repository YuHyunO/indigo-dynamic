package mb.dnm.service.general;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.SizeCheckable;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Input이 Null이거나 Empty 인 경우 프로세스를 중단한다
 *
 * <br>
 * <br>
 * *<b>Input</b>: null / empty / 0 을 체크할 input 파라미터명<br>
 * *<b>Input type</b>: {@link Map}, {@link Collection}, {@code Array}, {@link Number}, {@link SizeCheckable}
 * <br>
 * <br>
 * *<b>Output</b>: 조건이 부합하는 경우 output 할 파라미터명<br>
 * *<b>Output type</b>: {@code Object}<br>
 *
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.general.StopIfInputIsNullOrEmpty"&gt;
 *     &lt;property name="input"                 value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 *     &lt;property name="outputValue"           value="<span style="color: black; background-color: #FAF3D4;">input 파라미터 값</span>"/&gt;
 * &lt;/bean&gt;</pre>
 */
@Slf4j
@Setter
public class StopIfInputIsNullOrEmpty extends ParameterAssignableService implements Serializable {
    private static final long serialVersionUID = -3474809894612842750L;
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

    /**
     * Sets output value.
     *
     * @param outputValue the output value
     */
    public void setOutputValue(Object outputValue) {
        this.outputValue = outputValue;
    }
}
