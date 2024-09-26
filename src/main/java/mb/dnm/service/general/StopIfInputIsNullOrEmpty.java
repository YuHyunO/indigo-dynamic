package mb.dnm.service.general;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.EmptyCheckable;
import mb.dnm.access.db.DataSourceProvider;
import mb.dnm.access.db.QueryExecutor;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.core.context.TransactionContext;
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
 * @InputType <code>Map</code> or <code>Collection</code> or <code>EmptyCheckable</code>
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
        if (getOutput() != null) {
            setOutputValue(outputValue);
        }

        if (inputVal == null) {
            ctx.setProcessOn(false);
            log.info("[{}]Input value is null. The service process will be stop", ctx.getTxId());
            return;
        }

        if (inputVal instanceof Collection) {
            if (((Collection) inputVal).isEmpty()) {
                ctx.setProcessOn(false);
                log.info("[{}]Input value is empty. The service process will be stop", ctx.getTxId());
                return;
            }
        }

        if (inputVal instanceof Map) {
            if (((Map) inputVal).isEmpty()) {
                ctx.setProcessOn(false);
                log.info("[{}]Input value is empty. The service process will be stop", ctx.getTxId());
                return;
            }
        }

        if (inputVal instanceof EmptyCheckable) {
            if (((EmptyCheckable) inputVal).isEmpty()) {
                ctx.setProcessOn(false);
                log.info("[{}]Input value is empty. The service process will be stop", ctx.getTxId());
                return;
            }
        }

    }
}
