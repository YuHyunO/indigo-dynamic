package mb.dnm.exeption.handler;

import lombok.Setter;
import mb.dnm.core.ErrorHandler;

import java.util.List;

/**
 * The type Abstract error handler.
 */
@Setter
public abstract class AbstractErrorHandler implements ErrorHandler {
    private List<Class> exceptionToHandle;
    private boolean handleAllExceptions = true;

    @Override
    public boolean isTriggered(Class clazz) {
        if (handleAllExceptions) {
            return true;
        }
        return exceptionToHandle.contains(clazz);
    }

    /**
     * Sets exception to handle.
     *
     * @param exceptionToHandle the exception to handle
     */
    public void setExceptionToHandle(List<Class> exceptionToHandle) {
        this.handleAllExceptions = false;
        this.exceptionToHandle = exceptionToHandle;
    }

}
