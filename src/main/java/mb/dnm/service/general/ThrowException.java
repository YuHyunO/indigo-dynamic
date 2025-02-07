package mb.dnm.service.general;

import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.AbstractService;

/**
 * Exception 을 throw 한다.<br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.general.ThrowException"&gt;
 *     &lt;property name="msg"                 value="<span style="color: black; background-color: #FAF3D4;">Exception 메시지</span>"/&gt;
 * &lt;/bean&gt;</pre>
 */
public class ThrowException extends AbstractService {
    private String msg = "ThrowException service throws an exception";

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        throw new Exception(msg);
    }

    /**
     * Sets msg.
     *
     * @param msg the msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
