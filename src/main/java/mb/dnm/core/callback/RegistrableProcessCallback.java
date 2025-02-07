package mb.dnm.core.callback;

import mb.dnm.core.ServiceProcessor;

/**
 * The type Registrable process callback.
 */
public abstract class RegistrableProcessCallback implements AfterProcessCallback {
    private boolean registered = false;

    /**
     * 이 callback을 {@link ServiceProcessor}에 등록한다.
     */
    public void register() {
        if (!registered) {
            ServiceProcessor.addCallback(this);
            registered = true;
        }
    }
}
