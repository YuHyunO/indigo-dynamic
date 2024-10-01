package mb.dnm.access.dynamic;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.dynamic.DynamicCodeCompiler;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class DynamicCodeProvider {
    private static DynamicCodeProvider instance;
    private boolean initialized = false;
    private Resource[] codeLocations;
    private Map<String, DynamicCodeInstance> dnmCodes;


    /*
     * Spring version 만 맞다면 private 으로 변경해도 bean으로 등록 가능함
     * */
    public DynamicCodeProvider() {
        if (instance == null) {
            instance = this;
            dnmCodes = new HashMap<>();
        }

    }

    public static DynamicCodeProvider access() {
        if (instance == null) {
            new DynamicCodeProvider();
        }
        return instance;
    }

    public void setCodeLocations(Resource[] codeLocations) throws Exception {
        if (!initialized) {
            this.codeLocations = codeLocations;
            for (Resource resource : codeLocations) {
                List<DynamicCodeInstance> dncInstances = DynamicCodeCompiler.compile(resource);
                /*for (DynamicCodeInstance dncInstance : dncInstances) {
                    String dncId = dncInstance.getId();
                    if (dnmCodes.containsKey(dncId)) {
                        throw new IllegalStateException("Duplicated dynamic code id: " + dncId);
                    }
                    dnmCodes.put(dncId, dncInstance);
                    log.debug("Dynamic code with id '{}' is loaded", dncId);
                }*/
            }
            if (codeLocations.length == 0) {
                log.debug("No dynamic code locations are found");
            }
            initialized = true;
            return;
        }
        throw new IllegalStateException("DynamicCodeProvider is already initialized");
    }

    public DynamicCodeInstance getDynamicCode(String dnm) {
        return dnmCodes.get(dnm);
    }


}
