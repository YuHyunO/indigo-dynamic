package mb.dnm.service.general;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.AbstractService;

@Slf4j
public class PauseProcess extends AbstractService {
    private int millisecond = 0;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (millisecond == 0) return;

        log.info("[{}]Pausing process for {} ms ...", ctx.getTxId(), millisecond);
        Thread.sleep(millisecond);
        log.info("[{}]Resume process", ctx.getTxId());
    }

    public void setMillisecond(int millisecond) {
        if (millisecond < 0)
            millisecond = 0;
        this.millisecond = millisecond;
    }
}
