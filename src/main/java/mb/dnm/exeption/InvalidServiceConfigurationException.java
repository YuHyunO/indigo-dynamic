package mb.dnm.exeption;

import mb.dnm.core.Service;

import java.io.Serializable;

public class InvalidServiceConfigurationException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 8187415640085524789L;

    public InvalidServiceConfigurationException() {
        super("Service configuration is invalid");
    }

    public InvalidServiceConfigurationException(Class<? extends Service> serviceClass, String message) {
        super("Exception occurred in " + serviceClass + ". " + message);
    }

    public InvalidServiceConfigurationException(Class<? extends Service> serviceClass, Throwable cause) {
        super("Exception occurred in " + serviceClass + ". ", cause);
    }

    public InvalidServiceConfigurationException(Class<? extends Service> serviceClass, String message, Throwable cause) {
        super("Exception occurred in " + serviceClass + ". " + message, cause);
    }

}
