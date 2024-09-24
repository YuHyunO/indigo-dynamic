package mb.dnm.service.general;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

import java.util.Collection;

/**
 * <code>IterationGroup</code> 서비스에서 사용 가능한 서비스이다.<br>
 * input 파라미터로 들어온 값이 Null 또는 Empty 인 경우(Iterable 객체일 때) Iteration을 멈춘다.
 *
 * @see mb.dnm.service.general.IterationGroup
 *
 * @author Yuhyun O
 * @version 2024.09.22
 * @Input
 * @InputType <code>String</code>
 * */
@Slf4j
@Setter
public class BreakIteration extends ParameterAssignableService {

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            log.info("[{}]The input parameter is null. Breaking iteration", ctx.getTxId());
            ctx.addContextParam("$iter_break", true);
        }
        Object inputVal = getInputValue(ctx);

        if (inputVal == null) {
            log.info("[{}]The value of input parameter '{}' is null. Breaking iteration", ctx.getTxId(), getInput());
            ctx.addContextParam("$iter_break", true);
            return;
        } else if (inputVal instanceof Iterable) {
            if ( !((Iterable)inputVal).iterator().hasNext() ) {
                log.info("[{}]The value of input parameter '{}' is empty. Breaking iteration", ctx.getTxId(), getInput());
                ctx.addContextParam("$iter_break", true);
            }
        } else {
            log.warn("[{}]The input parameter is not iterable. Stop iteration", ctx.getTxId());
            ctx.addContextParam("$iter_break", true);
        }
    }

}
