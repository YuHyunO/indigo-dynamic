package mb.dnm.core.dynamic;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.core.context.ServiceContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Yuhyun O
 * @version 2024.10.01
 *
 * */
@Setter
@Getter
class DynamicCodeHolder {
    private String namespace;
    private String codeId;
    private String source;
    private Set<String> imports;
    private Class<? extends DynamicCode> wrapperClass;

    DynamicCodeHolder() {
        imports = new LinkedHashSet<>();
    }

    String getUniqueId() {
        return namespace + "." + codeId;
    }

    String getClassName() {
        StringBuilder className = new StringBuilder();
        className.append(wrapperClass.getSimpleName());
        className.append('$');
        className.append(hashString(namespace));
        className.append('$');
        className.append(hashString(codeId));

        return className.toString();
    }

    void addImport(Class importClass) {
        imports.add("import " + importClass.getName() + ";");
    }

    void addImports(Set<Class> importClasses) {
        for (Class importClass : importClasses) {
            imports.add("import " + importClass.getName() + ";");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-namespace = ").append(namespace).append(",\n");
        sb.append("-implements = ").append(wrapperClass.getName()).append(",\n");
        sb.append("-codeId = ").append(codeId).append(",\n");
        sb.append("-imports = [").append("\n");
        for (String importClass : imports) {
            sb.append("\t").append(importClass).append(",\n");
        }
        if (imports.size() > 0)
            sb.deleteCharAt(sb.length() - 2);
        sb.append("]").append(",\n");
        sb.append("-source = ").append("\n").append(source);

        return sb.toString();
    }

    public void setWrapperClass(Class<? extends DynamicCode> wrapperClass) throws Exception {
        if (!DynamicCode.class.isAssignableFrom(wrapperClass)) {
            throw new IllegalArgumentException("The class " + wrapperClass.getName() + " is not a subclass of " + DynamicCode.class.getName());
        }
        if (!Modifier.isAbstract(wrapperClass.getModifiers()))
            throw new IllegalArgumentException("The dynamic code wrapper class must be abstract. The class is not abstract class. " + wrapperClass.getName());
        try {
            Method method = wrapperClass.getDeclaredMethod("execute", ServiceContext.class);
            if (!method.toString().contains("abstract")) {
                throw new IllegalArgumentException("The dynamic code wrapper class's 'execute(ServiceContext ctx)' method must be abstract. Invalid class: " + wrapperClass.getName());
            }
        } catch (Exception e) {
            throw e;
        }
        addImport(wrapperClass.getClass());
        this.wrapperClass = (Class<? extends DynamicCode>) wrapperClass;
    }

    private String hashString(String s) {
        int hash = s.hashCode();
        if (hash < 0) {
            return String.valueOf("m" + -hash);
        } else {
            return String.valueOf(hash);
        }
    }
}
