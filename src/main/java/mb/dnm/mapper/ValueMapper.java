package mb.dnm.mapper;

import java.util.HashMap;

public class ValueMapper extends Mapper {
    private boolean createNullKey = true;
    private boolean nullToEmptyString = false;

    public ValueMapper() {
        map = new HashMap<>();
    }

    public void setCreateNullKey(boolean createNullKey) {
        this.createNullKey = createNullKey;
    }

    public void setNullToEmptyString(boolean nullToEmptyString) {
        this.nullToEmptyString = nullToEmptyString;
    }

    @Override
    public void addMap(String key, Object value) {
        map.put(key, value);
    }

    @Override
    public Object getMapped(String toMapped) {
        return null;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ValueMapper{");
        sb.append("id='").append(id).append('\'');
        sb.append(", create_null_key=").append(createNullKey);
        sb.append(", null_to_empty_string=").append(nullToEmptyString);
        sb.append(", map=").append(map);
        sb.append('}');
        return sb.toString();
    }
}
