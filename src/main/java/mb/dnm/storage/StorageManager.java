package mb.dnm.storage;

import mb.dnm.core.ErrorHandler;
import mb.dnm.core.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Storage manager.
 */
@Slf4j
public class StorageManager implements Serializable {
    private static final long serialVersionUID = 8454662346692851741L;
    private static StorageManager instance;
    private Map<String, InterfaceInfo> interfaceRegistry;
    private Map<String, List<Service>> serviceRegistry;
    private Map<String, List<ErrorHandler>> errorHandlerRegistry;
    private Map<String, String> httpRequestMappingRegistry;
    private boolean httpInterfaceEnabled = true;
    private boolean defaultInterfaceEnabled = true;

    /**
     * 새로운 {@code StorageManager} 객체를 생성한다. 인스턴스가 이미 존재하는 경우에는 생성하지 않는다.
     */
    public StorageManager() {
        if (StorageManager.instance == null) {
            instance = this;
            instance.interfaceRegistry = new HashMap<>();
            instance.serviceRegistry = new HashMap<>();
            instance.errorHandlerRegistry = new HashMap<>();
            instance.httpRequestMappingRegistry = new HashMap<>();
        }
    }


    /**
     * {@code StorageManager} 객체에 접근한다.
     *
     * @return the storage manager
     */
    public static StorageManager access() {
        if (StorageManager.instance == null) {
            new StorageManager();
        }
        return StorageManager.instance;
    }

    /**
     * {@code StorageManager}의 InterfaceRegistry 에 {@code InterfaceInfo} 객체들을 등록한다.
     *
     * @param interfaceInfos the interface infos
     */
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

    /**
     * {@code StorageManager}의 ServiceRegistry 에 Service-Strategies 를 등록한다.
     *
     * @param services the services
     */
    public void setServiceRegistry(Map<String, List<Service>> services){
        for(String id : services.keySet()){
            List<Service> serviceList = services.get(id);
            if (serviceList.isEmpty())
                throw new IllegalArgumentException("Service list for service id '" + id + "' is empty");
            this.serviceRegistry.put(id, serviceList);
        }
    }

    /**
     * {@code StorageManager}의 ErrorHandlerRegistry 에 ErrorHandlers 를 등록한다.
     *
     * @param errorHandlers the error handlers
     */
    public void setErrorHandlerRegistry(Map<String, List<ErrorHandler>> errorHandlers){
        for(String id : errorHandlers.keySet()){
            List<ErrorHandler> errorHandlerList = errorHandlers.get(id);
            if (errorHandlerList.isEmpty())
                throw new IllegalArgumentException("ErrorHandler list for errorHandler id '" + id + "' is empty");
            this.errorHandlerRegistry.put(id, errorHandlerList);
        }
    }

    /**
     * 인터페이스 ID 를 사용하여 {@code StorageManager}에 등록된 {@link InterfaceInfo} 객체를 가져온다.
     *
     * @param interfaceId the interface id
     * @return {@code InterfaceInfo} 객체, 존재하지않는 경우 null
     * @see InterfaceInfo
     * @see InterfaceInfo#getInterfaceId()
     */
    public InterfaceInfo getInterfaceInfo(String interfaceId) {
        if (interfaceId == null)
            return null;
        return instance.interfaceRegistry.get(interfaceId);
    }

    /**
     * serviceId 를 사용하여 {@code StorageManager}에 등록된 ServiceStrategy 를 가져온다.
     *
     * @param id ID of serviceStrategy
     * @return serviceStrategy
     * @see InterfaceInfo#getServiceId()
     */
    public List<Service> getServices(String id) {
        if (id == null)
            return null;
        return instance.serviceRegistry.get(id);
    }

    /**
     * errorHandlerId 를 사용하여 {@code StorageManager}에 등록된 ErrorHandler 를 가져온다.
     *
     * @param id errorHandlerId
     * @return ErrorHandler
     * @see InterfaceInfo#getErrorHandlerId()
     */
    public List<ErrorHandler> getErrorHandlersById(String id) {
        if (id == null)
            return null;
        return instance.errorHandlerRegistry.get(id);
    }

    /**
     * 인터페이스를 활성화 한다.
     *
     * @param interfaceId the interface id
     * @return The result of activation. It returns false when an InterfaceInfo is not exist.
     */
    public synchronized boolean activateInterface(String interfaceId) {
        if (interfaceId == null)
            return false;
        getInterfaceInfo(interfaceId).setActivated(true);
        return true;
    }

    /**
     * 인터페이스를 비활성화 한다.
     *
     * @param interfaceId the interface id
     * @return The result of activation. It returns false when an InterfaceInfo is not exist.
     */
    public synchronized boolean inactivateInterface(String interfaceId) {
        if (interfaceId == null)
            return false;
        getInterfaceInfo(interfaceId).setActivated(false);
        return true;
    }

    /**
     * Gets interface info of http request.
     *
     * @param url    the url
     * @param method the method
     * @return the interface info of http request
     */
    public InterfaceInfo getInterfaceInfoOfHttpRequest(String url, String method) {
        if (httpRequestMappingRegistry.containsKey(url)) {
            String interfaceId = httpRequestMappingRegistry.get(url);
            return getInterfaceInfo(interfaceId);

        } else {
            return null;
        }
    }

    /**
     * Sets default interface enabled.
     *
     * @param defaultInterfaceEnabled the default interface enabled
     */
    public void setDefaultInterfaceEnabled(boolean defaultInterfaceEnabled) {
        this.defaultInterfaceEnabled = defaultInterfaceEnabled;
    }

    /**
     * Gets interface infos.
     *
     * @return the interface infos
     */
    public List<InterfaceInfo> getInterfaceInfos() {
        List<InterfaceInfo> interfaceInfos = new ArrayList<>();
        interfaceInfos.addAll(instance.interfaceRegistry.values());
        return interfaceInfos;
    }
}
