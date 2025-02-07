package mb.dnm.core.dynamic;

import mb.dnm.core.context.ServiceContext;

/**
 * 모든 DynamicCode 의 인터페이스이다.
 */
public interface DynamicCode {
    /**
     * {@code DynamicCode} 를 실행한다.
     *
     * @param ctx the ctx
     * @throws Throwable the throwable
     */
    public void execute(ServiceContext ctx) throws Throwable;
}
