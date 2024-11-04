package mb.dnm.access.dynamic;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.dynamic.DynamicCodeCompiler;
import mb.dnm.core.dynamic.DynamicCodeInstance;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Yuhyun O
 * @version 2024.10.01
 *
 * */
@Slf4j
public class DynamicCodeProvider {
    private static DynamicCodeProvider instance;
    private boolean initialized = false;
    private Resource[] codeLocations;
    private Map<String, DynamicCodeInstance> dnmCodes;
    private String javacToolsPath = System.getProperty("java.home") + File.separator + "..//bin/java";
    private boolean standaloneMode = false;
    private boolean initializingLock = false;
    private int compilerThreadCount = 0;

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
        boolean error = false;

        if (!initialized) {
            initializingLock = true;
            if (codeLocations.length == 0) {
                log.debug("No dynamic code locations are found");
                return;
            }
            this.codeLocations = codeLocations;
            DynamicCodeCompiler.init();
            List<DynamicCodeInstance> dncInstances = null;
            if (compilerThreadCount > 0) {
                dncInstances = DynamicCodeCompiler.compileAll(this.codeLocations, compilerThreadCount);
            } else {
                dncInstances = DynamicCodeCompiler.compileAll(this.codeLocations);
            }
            log.debug("Generating DynamicCodeInstances ...");
            for (DynamicCodeInstance dncInstance : dncInstances) {
                dnmCodes.put(dncInstance.getId(), dncInstance);
                log.debug("Loaded dynamic code instance '{}.class' with id '{}'", dncInstance.getDynamicCodeClassName(), dncInstance.getId());
            }
            initialized = true;
            return;
        }
        throw new IllegalStateException("DynamicCodeProvider is already initialized");

    }

    public void setJavacToolsPaths(String javacToolsPath) {

        this.javacToolsPath = javacToolsPath;
    }

    public DynamicCodeInstance getDynamicCode(String dnm) {
        return dnmCodes.get(dnm);
    }

    public void setStandaloneMode(boolean standaloneMode) throws Exception {
        this.standaloneMode = standaloneMode;
        DynamicCodeCompiler.getInstance().setStandaloneMode(standaloneMode);
    }

    public void setCompilerThreadCount(int compilerThreadCount) {
        if (compilerThreadCount < 0) {
            compilerThreadCount = 0;
        }
        this.compilerThreadCount = compilerThreadCount;
    }

    public int getCompilerThreadCount() {
        return compilerThreadCount;
    }
}
