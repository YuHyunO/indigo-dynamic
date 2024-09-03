package com.mb.dnm.storage;

import com.mb.dnm.core.ErrorHandler;
import com.mb.dnm.core.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class StorageManager {
    private static StorageManager instance;
    private Map<String, InterfaceInfo> interfaceRegistry;
    private Map<String, List<Service>> serviceRegistry;
    private Map<String, List<ErrorHandler>> errorHandlerRegistry;

    private StorageManager() {
        if (StorageManager.instance == null) {
            instance = this;
            instance.interfaceRegistry = new HashMap<>();
            instance.serviceRegistry = new HashMap<>();
            instance.errorHandlerRegistry = new HashMap<>();
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
            this.interfaceRegistry.put(ifId, info);
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
    public boolean activateInterface(String interfaceId) {
        if (interfaceId == null)
            return false;
        getInterfaceInfo(interfaceId).setActivated(true);
        return true;
    }

    /**
     * @return The result of activation. It returns false when an InterfaceInfo is not exist.
     * */
    public boolean inactivateInterface(String interfaceId) {
        if (interfaceId == null)
            return false;
        getInterfaceInfo(interfaceId).setActivated(false);
        return true;
    }

}
