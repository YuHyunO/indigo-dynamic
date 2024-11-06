package mb.dnm.service.http;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.dispatcher.http.HttpRequestDispatcher;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;

import java.io.Serializable;

/**
 * Input으로 전달된 데이터를 HTTP Response Body로 지정한다.
 * <br>
 * HTTP Response Body로 지정된 데이터는 <code>HttpRequestDispatcher</code>에서 HTTP Client 에게 응답된다.<br>
 * Context-Type 이나 Content-Encoding 은 <code>InterfaceInfo#HttpAPITemplate</code> 의 설정을 따른다.
 *
 * @see mb.dnm.dispatcher.http.HttpRequestDispatcher
 * @see mb.dnm.access.http.HttpAPITemplate
 *
 * @author Yuhyun O
 * @version 2024.09.30
 * @Input HTTP 응답 바디 데이터
 * @InputType <code>Object</code>
 *
 * */
@Slf4j
@Setter
public class SetHttpResponseBody extends ParameterAssignableService implements Serializable {

    private static final long serialVersionUID = 2461342063038762900L;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            return;
        }

        Object inputVal = getInputValue(ctx);

        if (inputVal != null) {
            ctx.addContextParam(HttpRequestDispatcher.RESPONSE_BODY, inputVal);
        }
    }


}
