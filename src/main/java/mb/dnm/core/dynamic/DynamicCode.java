package mb.dnm.core.dynamic;

import mb.dnm.core.context.ServiceContext;

/**
 *
 *
 * @author Yuhyun O
 * @version 2024.10.01
 *
 * */
public interface DynamicCode {
    public void execute(ServiceContext ctx) throws Throwable;
}
