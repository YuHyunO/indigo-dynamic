package mb.dnm.service.general;

import lombok.Setter;
import mb.dnm.access.SizeCheckable;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.ParameterAssignableService;
import org.apache.commons.beanutils.ConvertUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * {@code outputValue}를 {@code output} 속성에 지정한 명칭으로 {@link ServiceContext#addContextParam(String, Object)} 한다.
 *
 * <br>
 * <br>
 * *<b>Output</b>: output 할 파라미터명<br>
 * *<b>Output type</b>: {@code Object}<br>
 *
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.general.OutputCustomData"&gt;
 *     &lt;property name="output"                            value=""/&gt;
 *     &lt;property name="customData"                        value=""/&gt;
 * &lt;/bean&gt;</pre>
 */
public class OutputCustomData extends ParameterAssignableService implements Serializable {
    private static final long serialVersionUID = 6380142655583465081L;
    private Object customData = null;
    private Class castType = null;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        setOutputValue(ctx, customData);
    }

    /**
     * Sets custom data.
     *
     * @param customData the custom data
     */
    public void setCustomData(Object customData) {
        if (castType != null) {
            this.customData = ConvertUtils.convert(customData, castType);
        } else {
            this.customData = customData;
        }
    }

    /**
     * Sets cast class type.
     *
     * @param castType the cast type
     */
    public void setCastClassType(Class castType) {
        this.castType = castType;
    }

    /**
     * Sets cast type.
     *
     * @param castType the cast type
     * @throws ClassNotFoundException the class not found exception
     * @throws ClassCastException     the class cast exception
     */
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
