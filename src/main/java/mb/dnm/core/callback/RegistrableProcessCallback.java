package mb.dnm.core.callback;

import mb.dnm.core.ServiceProcessor;

public abstract class RegistrableProcessCallback implements AfterProcessCallback {
    private boolean registered = false;

    public void register() {
        if (!registered) {
            ServiceProcessor.addCallback(this);
            registered = true;
        }
    }
}
