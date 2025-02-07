package mb.dnm.core;

import mb.dnm.core.context.ServiceContext;

/**
 * The interface Service.
 */
public interface Service {

    /**
     * {@code Service} 의 고유한 기능을 수행한다.
     *
     * @param ctx 하나의 service processing 을 관통하는 {@link ServiceContext} 객체
     * @throws Throwable the throwable
     */
    public void process(ServiceContext ctx) throws Throwable;

    /**
     * Sets ignore error.
     *
     * @param ignoreError the ignore error
     */
    public void setIgnoreError(boolean ignoreError);

    /**
     * Is ignore error boolean.
     *
     * @return the boolean
     */
    public boolean isIgnoreError();

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description);

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription();

}
