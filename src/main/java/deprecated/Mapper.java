package deprecated;

import java.lang.reflect.Method;
import java.util.Map;

@Deprecated
public abstract class Mapper {
    protected String id;
    protected Map<String, Object> map;
    protected Map<String, Method> functions; //cached at initialization

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract void addMap(String key, Object value);

    public abstract Object getMapped(String toMapped);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("id=").append(id);
        sb.append("]");
        return sb.toString();
    }
}
