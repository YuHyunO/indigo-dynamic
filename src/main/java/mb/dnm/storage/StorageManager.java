package mb.dnm.storage;

import mb.dnm.core.ErrorHandler;
import mb.dnm.core.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class StorageManager {
    private static StorageManager instance;
    private Map<String, InterfaceInfo> interfaceRegistry;
    private Map<String, List<Service>> serviceRegistry;
    private Map<String, List<ErrorHandler>> errorHandlerRegistry;
    private Map<String, String> httpRequestMappingRegistry;
    private boolean httpInterfaceEnabled = true;
    private boolean defaultInterfaceEnabled = true;

    public StorageManager() {
        if (StorageManager.instance == null) {
            instance = this;
            instance.interfaceRegistry = new HashMap<>();
            instance.serviceRegistry = new HashMap<>();
            instance.errorHandlerRegistry = new HashMap<>();
            instance.httpRequestMappingRegistry = new HashMap<>();
        }
    }

    public static StorageManager access() {
        if (StorageManager.instance == null) {
            new StorageManager();
        }
        return StorageManager.instance;
    }

    public void setInterfaceRegistry(List<InterfaceInfo> interfaceInfos){
        for(InterfaceInfo info : interfaceInfos){
            String ifId = info.getInterfaceId();

            if (!defaultInterfaceEnabled && !info.isControllerInterface()) {
                info.setActivated(false);
            }

            this.interfaceRegistry.put(ifId, info);
            if (httpInterfaceEnabled) {
                String frontUrl = info.getFrontHttpUrl();
                if (frontUrl != null) {
                    frontUrl = frontUrl.trim();
                    if (!frontUrl.isEmpty()) {
                        if (!frontUrl.startsWith("/")) {
                            frontUrl = "/" + frontUrl;
                        }

                        if (frontUrl.contains("?") || frontUrl.contains("&")) {
                            throw new IllegalArgumentException("Invalid front url path. Illegal characters in frontHttpUrl path '?' or '&': " + frontUrl);
                        }
                        frontUrl = frontUrl.replace("@{if_id}", ifId);

                        if (httpRequestMappingRegistry.containsKey(frontUrl)) {
                            throw new IllegalArgumentException("Duplicate front url '" + frontUrl + "' of Interface id: " + ifId);
                        }
                        httpRequestMappingRegistry.put(frontUrl, ifId);

                    }
                }
            }
        }

        if (httpInterfaceEnabled) {
            log.debug("The HTTP request mapping registry has been initialized.");
            if (httpRequestMappingRegistry.size() > 0) {
                for (Map.Entry<String, String> entry : httpRequestMappingRegistry.entrySet()) {
                    log.debug("HTTP request mapping: url: " + entry.getKey() + " -> interface id: " + entry.getValue());
                }
            } else {
                log.debug("No HTTP request routing set is exist.");
            }
        }

    }

    public void setServiceRegistry(Map<String, List<Service>> services){
        for(String id : services.keySet()){
            List<Service> serviceList = services.get(id);
            if (serviceList.isEmpty())
                throw new IllegalArgumentException("Service list for service id '" + id + "' is empty");
            this.serviceRegistry.put(id, serviceList);
        }
    }

    public void setErrorHandlerRegistry(Map<String, List<ErrorHandler>> errorHandlers){
        for(String id : errorHandlers.keySet()){
            List<ErrorHandler> errorHandlerList = errorHandlers.get(id);
            if (errorHandlerList.isEmpty())
                throw new IllegalArgumentException("ErrorHandler list for errorHandler id '" + id + "' is empty");
            this.errorHandlerRegistry.put(id, errorHandlerList);
        }
    }

    /**
     * Get an InterfaceInfo by id
     * */
    public InterfaceInfo getInterfaceInfo(String interfaceId) {
        if (interfaceId == null)
            return null;
        return instance.interfaceRegistry.get(interfaceId);
    }

    /**
     * Get a service list by id
     * */
    public List<Service> getServices(String id) {
        if (id == null)
            return null;
        return instance.serviceRegistry.get(id);
    }

    /**
     * Get an error handler list by id
     * */
    public List<ErrorHandler> getErrorHandlersById(String id) {
        if (id == null)
            return null;
        return instance.errorHandlerRegistry.get(id);
    }

    /**
     * @return The result of activation. It returns false when an InterfaceInfo is not exist.
     * */
    public synchronized boolean activateInterface(String interfaceId) {
        if (interfaceId == null)
            return false;
        getInterfaceInfo(interfaceId).setActivated(true);
        return true;
    }

    /**
     * @return The result of activation. It returns false when an InterfaceInfo is not exist.
     * */
    public synchronized boolean inactivateInterface(String interfaceId) {
        if (interfaceId == null)
            return false;
        getInterfaceInfo(interfaceId).setActivated(false);
        return true;
    }

    public InterfaceInfo getInterfaceInfoOfHttpRequest(String url, String method) {
        if (httpRequestMappingRegistry.containsKey(url)) {
            String interfaceId = httpRequestMappingRegistry.get(url);
            return getInterfaceInfo(interfaceId);

        } else {
            return null;
        }
    }

    public void setDefaultInterfaceEnabled(boolean defaultInterfaceEnabled) {
        this.defaultInterfaceEnabled = defaultInterfaceEnabled;
    }

    public List<InterfaceInfo> getInterfaceInfos() {
        List<InterfaceInfo> interfaceInfos = new ArrayList<>();
        interfaceInfos.addAll(instance.interfaceRegistry.values());
        return interfaceInfos;
    }
}
