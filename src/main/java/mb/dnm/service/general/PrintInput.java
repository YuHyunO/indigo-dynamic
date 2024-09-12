package mb.dnm.service.general;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;


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
public class PrintInput extends ParameterAssignableService {

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() != null) {
            log.info("Input: {}, Input value: {}", getInput(), getInputValue(ctx));
        }
    }

}
