package mb.dnm.core;

import mb.dnm.core.context.ServiceContext;

/**
 * The interface Error handler.
 */
public interface ErrorHandler {

    /**
     * Is triggered boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    public boolean isTriggered(Class<? extends Throwable> clazz);

    /**
     * Handle error.
     *
     * @param ctx the ctx
     * @throws Throwable the throwable
     */
    public void handleError(ServiceContext ctx) throws Throwable;



}

