package mb.dnm.exeption;

import mb.dnm.core.Service;

import java.io.Serializable;

/**
 * The type Invalid service configuration exception.
 */
public class InvalidServiceConfigurationException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 8187415640085524789L;

    /**
     * Instantiates a new Invalid service configuration exception.
     */
    public InvalidServiceConfigurationException() {
        super("Service configuration is invalid");
    }

    /**
     * Instantiates a new Invalid service configuration exception.
     *
     * @param serviceClass the service class
     * @param message      the message
     */
    public InvalidServiceConfigurationException(Class<? extends Service> serviceClass, String message) {
        super("Exception occurred in " + serviceClass + ". " + message);
    }

    /**
     * Instantiates a new Invalid service configuration exception.
     *
     * @param serviceClass the service class
     * @param cause        the cause
     */
    public InvalidServiceConfigurationException(Class<? extends Service> serviceClass, Throwable cause) {
        super("Exception occurred in " + serviceClass + ". ", cause);
    }

    /**
     * Instantiates a new Invalid service configuration exception.
     *
     * @param serviceClass the service class
     * @param message      the message
     * @param cause        the cause
     */
    public InvalidServiceConfigurationException(Class<? extends Service> serviceClass, String message, Throwable cause) {
        super("Exception occurred in " + serviceClass + ". " + message, cause);
    }

}
