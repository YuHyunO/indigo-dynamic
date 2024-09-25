package mb.dnm.service.general;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;


/**
 * Input 파라미터로 전달된 변수의 크기를 output 한다.<br><br>
 *
 * 변수의 타입에 따라 크기 확인은 다음과 같이 이루어진다.<br>
 * -<code>java.util.Collection</code>: <code>Collection#size()</code><br>
 * -<code>java.util.Map</code>: <code>Collection#size()</code><br>
 * -<code>Arrays</code>: <code>Array#length</code><br>
 * -<code>java.lang.CharSequence</code>: <code>CharSequence#length()</code><br>
 * -<code>java.lang.Number</code>: <code>value of itself</code><br>
 * -<code>java.lang.Character</code>: <code>value of itself</code><br>
 * -<code>Primitive types</code>: <code>value of itself</code><br>
 * -<code>Other objects or when null</code>: <code>0</code><br>
 *
 * @author Yuhyun O
 * @version 2024.09.25
 *
 * @Input 크기를 확인할 변수
 * @InputType <code>Object</code>
 * @Output 파라미터로 전달된 변수의 크기를 반환한다.
 * @OutputType <br>
 * -<code>java.util.Collection</code>: <code>Collection#size()</code><br>
 * -<code>java.util.Map</code>: <code>Collection#size()</code><br>
 * -<code>Arrays</code>: <code>Array#length</code><br>
 * -<code>java.lang.CharSequence</code>: <code>CharSequence#length()</code><br>
 * -<code>java.lang.Number</code>: <code>value of itself</code><br>
 * -<code>Primitive types</code>: <code>value of itself</code><br>
 * -<code>Other objects</code>: <code>0</code><br>
 *
 * */
@Slf4j
public class GetSize extends ParameterAssignableService {

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
