package mb.dnm.service.general;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;

/**
 * <code>IterationGroup</code> 서비스에서 사용 가능한 서비스이다.<br>
 * input 파라미터로 들어온 값이 whenInputEquals 의 값과 동일한 경우 현재 Iteration 을 건너뛴다.
 *
 * @see mb.dnm.service.general.IterationGroup
 *
 * @author Yuhyun O
 * @version 2024.11.25
 * @Input
 * @InputType <code>String</code>
 * */
@Slf4j
public class ContinueIteration extends ParameterAssignableService {
    protected String whenInputEquals;
    protected String whenInputNotEquals;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        Object inputVal = getInputValue(ctx);

        if (whenInputEquals != null) {
            if (whenInputEquals.equals(String.valueOf(inputVal))) {
                log.debug("[{}]Continue iteration", ctx.getTxId());
                ctx.addContextParam("$iter_continue", true);
            }
        } else if (whenInputNotEquals != null) {
            if (!whenInputNotEquals.equals(String.valueOf(inputVal))) {
                log.debug("[{}]Continue iteration", ctx.getTxId());
                ctx.addContextParam("$iter_continue", true);
                Object iterPosition = ctx.getContextParam("$iter_position");
            }
        }
    }

    public void setWhenInputEquals(String whenInputEquals) {
        this.whenInputNotEquals = null;
        this.whenInputEquals = whenInputEquals;
    }

    public void setWhenInputNotEquals(String whenInputNotEquals) {
        this.whenInputEquals = null;
        this.whenInputNotEquals = whenInputNotEquals;
    }
}
