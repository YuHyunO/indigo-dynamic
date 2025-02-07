package mb.dnm.service.general;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.AbstractService;

import java.io.Serializable;

/**
 * Service-Chaining을 {@code millisecond}만큼 일시중지한다.
 * <br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.general.PauseProcess"&gt;
 *     &lt;property name="millisecond"                 value="<span style="color: black; background-color: #FAF3D4;">millisecond</span>"/&gt;
 * &lt;/bean&gt;</pre>
 */
@Slf4j
public class PauseProcess extends AbstractService implements Serializable {
    private static final long serialVersionUID = -8689374111009769407L;
    private int millisecond = 0;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (millisecond == 0) return;

        log.info("[{}]Pausing process for {} ms ...", ctx.getTxId(), millisecond);
        Thread.sleep(millisecond);
        log.info("[{}]Resume process", ctx.getTxId());
    }

    /**
     * Sets millisecond.
     *
     * @param millisecond the millisecond
     */
    public void setMillisecond(int millisecond) {
        if (millisecond < 0)
            millisecond = 0;
        this.millisecond = millisecond;
    }
}
