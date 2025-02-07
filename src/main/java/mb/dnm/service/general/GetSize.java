package mb.dnm.service.general;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.SizeCheckable;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;


/**
 * Input 파라미터로 전달된 변수의 크기를 output 한다.<br><br>
 *
 * 변수의 타입에 따라 크기 확인은 다음과 같이 이루어진다.<br>
 * * {@code Collection}: {@code Collection#size()}<br>
 * * {@code Map}: {@code Map#size()}<br>
 * * {@code Array}: {@code Array#length}<br>
 * * {@code CharSequence}: {@code CharSequence#length()}<br>
 * * {@code Number}: {@code value of itself}<br>
 * * {@code Character}: {@code value of itself}<br>
 * * {@code Primitive types}: {@code value of itself}<br>
 * * {@code Other objects or when null}: {@code 0}<br>
 * <br>
 * <br>
 * *<b>Input</b>: 크기를 확인할 input 파라미터명<br>
 * *<b>Input type</b>: {@code Obct}
 * <br>
 * <br>
 * *<b>Output</b>: 타입별 객체의 크기<br>
 * *<b>Output type</b>:
 * * {@code Collection}: {@code Collection#size()}<br>
 * * {@code Map}: {@code Map#size()}<br>
 * * {@code Array}: {@code Array#length}<br>
 * * {@code CharSequence}: {@code CharSequence#length()}<br>
 * * {@code Number}: {@code value of itself}<br>
 * * {@code Character}: {@code value of itself}<br>
 * * {@code Primitive types}: {@code value of itself}<br>
 * * {@code Other objects or when null}: {@code 0}<br>
 *
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.general.GetSize"&gt;
 *     &lt;property name="input"                  value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                 value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 * &lt;/bean&gt;</pre>
 * */
@Slf4j
public class GetSize extends ParameterAssignableService implements Serializable {

    private static final long serialVersionUID = 3861612581954345962L;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        String inputName = getInput();
        Object inputVal = getInputValue(ctx);
        Object size = 0;

        if (inputName != null && getOutput() != null) {
            if (inputVal != null) {
                boolean couldnt = false;
                Class type = inputVal.getClass();
                if (Collection.class.isAssignableFrom(type)) {
                    size = ((Collection) inputVal).size();

                } else if (Map.class.isAssignableFrom(type)) {
                    size = ((Map) inputVal).size();

                } else if (type.isArray()) {
                    size = Arrays.asList(inputVal).size();

                } else if (type.isPrimitive()) {
                    size = inputVal;

                } else if (CharSequence.class.isAssignableFrom(type)) {
                    size = ((CharSequence) inputVal).length();

                } else if (Number.class.isAssignableFrom(type)) {
                    size = inputVal;

                } else if (SizeCheckable.class.isAssignableFrom(type)) {
                    size = ((SizeCheckable) inputVal).getSize();

                } else {
                    couldnt = true;
                }


                if (couldnt) {
                    log.debug("[{}]Couldn't get the size of input '{}'. Not supported input type: {}, Output: 0", ctx.getTxId(), inputName, type);
                } else {
                    log.debug("[{}]The size of input '{}' is detected. Input type: {}, Output: {}, Output type: {}", ctx.getTxId(), inputName, type, size, size.getClass());
                }

            } else {
                log.debug("[{}]The size of input '{}' is null. Output: 0", ctx.getTxId(), inputName);
            }

            setOutputValue(ctx, size);
        } else {
            log.debug("[{}]No input or output parameter is assigned in this service", ctx.getTxId());
        }

    }

}
