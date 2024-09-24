package mb.dnm.service.general;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.util.MessageUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Input 파라미터로 들어온 데이터를 로그로 출력한다.
 * 지정된 input 파라미터가 없는 경우 아무것도 출력되지 않는다.
 *
 * @see mb.dnm.service.ftp.FTPLogin
 *
 * @author Yuhyun O
 * @version 2024.09.12
 *
 * @Input List를 가져올 Directory의 경로
 * @InputType <code>Object</code>
 * */
@Slf4j
@Setter
public class PrintInput extends ParameterAssignableService {

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

    public void setPrintToJsonWhenCollectionOrMap(boolean printToJsonWhenCollectionOrMap) {
        this.printToXmlWhenCollectionOrMap = !printToJsonWhenCollectionOrMap;
        this.printToJsonWhenCollectionOrMap = printToJsonWhenCollectionOrMap;
    }

    public void setPrintToXmlWhenCollectionOrMap(boolean printToXmlWhenCollectionOrMap) {
        this.printToJsonWhenCollectionOrMap = !printToXmlWhenCollectionOrMap;
        this.printToXmlWhenCollectionOrMap = printToXmlWhenCollectionOrMap;
    }
}
