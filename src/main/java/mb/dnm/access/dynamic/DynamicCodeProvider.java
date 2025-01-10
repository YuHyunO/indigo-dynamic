package mb.dnm.access.dynamic;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.dynamic.DynamicCodeCompiler;
import mb.dnm.core.dynamic.DynamicCodeInstance;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Dynamic code provider.
 *
 * @author Yuhyun O
 * @version 2024.10.01
 */
@Slf4j
public class DynamicCodeProvider implements Serializable {
    private static final long serialVersionUID = 4285587685416187268L;
    private static DynamicCodeProvider instance;
    private boolean initialized = false;
    private Resource[] codeLocations;
    private Map<String, DynamicCodeInstance> dnmCodes;
    private String javacToolsPath = System.getProperty("java.home") + File.separator + "..//bin/java";
    private boolean standaloneMode = false;
    private boolean initializingLock = false;
    private int compilerThreadCount = 0;

    /**
     * Instantiates a new Dynamic code provider.
     */
    /*
     * Spring version 만 맞다면 private 으로 변경해도 bean으로 등록 가능함
     * */
    public DynamicCodeProvider() {
        if (instance == null) {
            instance = this;
            dnmCodes = new HashMap<>();
        }

    }

    /**
     * Access dynamic code provider.
     *
     * @return the dynamic code provider
     */
    public static DynamicCodeProvider access() {
        if (instance == null) {
            new DynamicCodeProvider();
        }
        return instance;
    }

    /**
     * Sets code locations.
     *
     * @param codeLocations the code locations
     * @throws Exception the exception
     */
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
                log.debug("Generating DynamicCodeInstances using threads ...");
                dncInstances = DynamicCodeCompiler.compileAll(this.codeLocations, compilerThreadCount);
            } else {
                log.debug("Generating DynamicCodeInstances ...");
                dncInstances = DynamicCodeCompiler.compileAll(this.codeLocations);
            }
            for (DynamicCodeInstance dncInstance : dncInstances) {
                dnmCodes.put(dncInstance.getId(), dncInstance);
                log.debug("Loaded dynamic code instance '{}.class' with id '{}'", dncInstance.getDynamicCodeClassName(), dncInstance.getId());
            }
            initialized = true;
            return;
        }
        throw new IllegalStateException("DynamicCodeProvider is already initialized");

    }

    /**
     * Sets javac tools paths.
     *
     * @param javacToolsPath the javac tools path
     */
    public void setJavacToolsPaths(String javacToolsPath) {

        this.javacToolsPath = javacToolsPath;
    }

    /**
     * Gets dynamic code.
     *
     * @param dnm the dnm
     * @return the dynamic code
     */
    public DynamicCodeInstance getDynamicCode(String dnm) {
        return dnmCodes.get(dnm);
    }

    /**
     * Sets standalone mode.
     *
     * @param standaloneMode the standalone mode
     * @throws Exception the exception
     */
    public void setStandaloneMode(boolean standaloneMode) throws Exception {
        this.standaloneMode = standaloneMode;
        DynamicCodeCompiler.getInstance().setStandaloneMode(standaloneMode);
    }

    /**
     * Sets compiler thread count.
     *
     * @param compilerThreadCount the compiler thread count
     */
    public void setCompilerThreadCount(int compilerThreadCount) {
        if (compilerThreadCount < 0) {
            compilerThreadCount = 0;
        }
        this.compilerThreadCount = compilerThreadCount;
    }

    /**
     * Gets compiler thread count.
     *
     * @return the compiler thread count
     */
    public int getCompilerThreadCount() {
        return compilerThreadCount;
    }
}
