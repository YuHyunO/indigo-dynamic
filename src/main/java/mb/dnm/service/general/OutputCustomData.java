package mb.dnm.service.general;

import lombok.Setter;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;
import org.apache.commons.beanutils.ConvertUtils;

import java.io.Serializable;

public class OutputCustomData extends ParameterAssignableService implements Serializable {
    private static final long serialVersionUID = 6380142655583465081L;
    private Object customData = null;
    private Class castType = null;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        setOutputValue(ctx, customData);
    }

    public void setCustomData(Object customData) {
        if (castType != null) {
            this.customData = ConvertUtils.convert(customData, castType);
        } else {
            this.customData = customData;
        }
    }

    public void setCastClassType(Class castType) {
        this.castType = castType;
    }

    public void setCastType(String castType) throws ClassNotFoundException, ClassCastException {
        castType = castType.trim();
        switch (castType) {
            case "int": case "Integer": castType = Integer.class.getName(); break;
            case "long": case "Long": castType = Long.class.getName(); break;
            case "float": case "Float": castType = Float.class.getName(); break;
            case "double": case "Double": castType = Double.class.getName(); break;
            case "boolean": case "Boolean": castType = Boolean.class.getName(); break;
            case "char": case "Character": castType = Character.class.getName(); break;
            case "String": castType = String.class.getName(); break;
            case "Object": case "java.lang.Object": return;
        }

        Class clazz = Class.forName(castType);
        setCastClassType(clazz);

    }

}
