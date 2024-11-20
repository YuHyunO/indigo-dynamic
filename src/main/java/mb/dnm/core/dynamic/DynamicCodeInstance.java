package mb.dnm.core.dynamic;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.core.context.ServiceContext;
import org.springframework.core.io.Resource;

import java.lang.reflect.Method;

/**
 *
 *
 * @author Yuhyun O
 * @version 2024.09.30
 *
 * */
@Slf4j
public class DynamicCodeInstance {
    private final Resource resource;
    private final String id;
    private final Class<? extends DynamicCode> loadedClass;
    private final Object instance;
    private final Method method;

    public DynamicCodeInstance(Resource resource, String id, Class<? extends DynamicCode> loadedClass) throws Exception {
        this.resource = resource;
        this.id = id;
        this.loadedClass = loadedClass;
        this.instance = loadedClass.newInstance();
        this.method = loadedClass.getMethod("execute", ServiceContext.class);
    }

    public void execute(ServiceContext ctx) throws Exception {
        if (instance != null) {
            method.invoke(instance, ctx);
            return;
        }
        throw new IllegalStateException("The dynamic code instance has not been created.");
    }

    public String getId() {
        return id;
    }

    public String getDynamicCodeClassName() throws Exception {
        return loadedClass.getName();
    }

    public Class<? extends DynamicCode> getLoadedClass() throws Exception {
        return loadedClass;
    }

    public Resource getResource() {
        return resource;
    }

}
