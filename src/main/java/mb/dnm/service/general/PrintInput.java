package mb.dnm.service.general;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.SizeCheckable;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.util.MessageUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Input 파라미터로 전달받은 데이터를 로그로 출력한다.
 * <br>
 * <br>
 * *<b>Input</b>: 로그를 출력할 context 파라미터명<br>
 * *<b>Input type</b>: {@link Object}<br>
 *
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.general.PrintInput"&gt;
 *     &lt;property name="input"                                      value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="printToJsonWhenCollectionOrMap"             value="<span style="color: black; background-color: #FAF3D4;">true or false</span>"/&gt;
 *     &lt;property name="printToXmlWhenCollectionOrMap"              value="<span style="color: black; background-color: #FAF3D4;">true or false</span>"/&gt;
 *     &lt;property name="indented"                                   value="<span style="color: black; background-color: #FAF3D4;">true or false</span>"/&gt;
 * &lt;/bean&gt;</pre>
 */
@Slf4j
@Setter
public class PrintInput extends ParameterAssignableService implements Serializable {

    private static final long serialVersionUID = 5801564941267138217L;
    /**
     * input으로 전달된 데이터의 타입이 Collection인 경우 json 형식으로 출력한다.
     * */
    private boolean printToJsonWhenCollectionOrMap = false;
    /**
     * input으로 전달된 데이터의 타입이 Collection인 경우 xml 형식으로 출력한다.
     * */
    private boolean printToXmlWhenCollectionOrMap = false;
    /**
     * <code>printToJsonWhenCollectionOrMap</code> 또는 <code>printToXmlWhenCollectionOrMap</code> 속성이 true인 경우 들여쓰기를 적용하여 출력할 지에 대한 여부
     * */
    private boolean indented = false;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        String inputName = getInput();
        if (inputName != null) {
            Object inputValue = getInputValue(ctx);
            if (inputValue != null) {
                if (printToJsonWhenCollectionOrMap) {
                    if (inputValue instanceof Collection || inputValue instanceof Map) {
                        Map<String, Object> wrapper = new HashMap<>();
                        wrapper.put(inputName, inputValue);
                        log.info("[{}]\n{}", ctx.getTxId(), MessageUtil.mapToJson(wrapper, indented));
                        return;
                    }
                } else if (printToXmlWhenCollectionOrMap) {
                    if (inputValue instanceof Collection || inputValue instanceof Map) {
                        Map<String, Object> wrapper = new HashMap<>();
                        wrapper.put(inputName, inputValue);
                        log.info("[{}]\n{}",  ctx.getTxId(), MessageUtil.mapToXml(wrapper, indented));
                        return;
                    }
                }
                log.info("[{}]Input: {}, Input type: {}, Input value: {}", ctx.getTxId(), getInput(), inputValue.getClass(), inputValue);
            } else {
                log.info("[{}]Input value of '{}' is null", ctx.getTxId(), inputName);
            }
        } else {
            log.info("[{}]No input is assigned", ctx.getTxId());
        }
    }

    /**
     * input의 타입이 {@link Collection} 또는 {@link Map} 인 경우 로그를 Json 형태로 출력할 지 여부
     *
     * @param printToJsonWhenCollectionOrMap the print to json when collection or map
     */
    public void setPrintToJsonWhenCollectionOrMap(boolean printToJsonWhenCollectionOrMap) {
        this.printToXmlWhenCollectionOrMap = !printToJsonWhenCollectionOrMap;
        this.printToJsonWhenCollectionOrMap = printToJsonWhenCollectionOrMap;
    }

    /**
     * input의 타입이 {@link Collection} 또는 {@link Map} 인 경우 로그를 XML 형태로 출력할 지 여부
     *
     * @param printToXmlWhenCollectionOrMap the print to xml when collection or map
     */
    public void setPrintToXmlWhenCollectionOrMap(boolean printToXmlWhenCollectionOrMap) {
        this.printToJsonWhenCollectionOrMap = !printToXmlWhenCollectionOrMap;
        this.printToXmlWhenCollectionOrMap = printToXmlWhenCollectionOrMap;
    }

    /**
     * {@code this.printToXmlWhenCollectionOrMap = true} 또는 {@code this.printToXmlWhenCollectionOrMap = true} 인 경우 들여쓰기 하여 출력할 지 여부
     *
     * @param indented the indented
     */
    public void setIndented(boolean indented) {
        this.indented = indented;
    }
}
