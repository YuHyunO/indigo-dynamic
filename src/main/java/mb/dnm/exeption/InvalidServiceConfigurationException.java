package mb.dnm.exeption;

import mb.dnm.core.Service;

public class InvalidServiceConfigurationException extends RuntimeException {

    public InvalidServiceConfigurationException() {
        super("Service configuration is invalid");
    }

    public InvalidServiceConfigurationException(Class<? extends Service> serviceClass, String message) {
        super("Exception occurred in " + serviceClass + ". " + message);
    }

}
