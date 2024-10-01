package mb.dnm.core.dynamic;

import mb.dnm.core.context.ServiceContext;

import java.lang.reflect.Method;

/**
 *
 *
 * @author Yuhyun O
 * @version 2024.09.30
 *
 * */
public class DynamicCodeInstance {
    private final String id;
    private final Class<? extends DynamicCode> loadedClass;
    private final Object instance;
    private final Method method;

    public DynamicCodeInstance(String id, Class<? extends DynamicCode> loadedClass, Object instance) throws Exception {
        this.id = id;
        this.loadedClass = loadedClass;
        this.instance = instance;
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


}
