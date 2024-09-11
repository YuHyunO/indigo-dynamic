package deprecated;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class MappingFactory {
    private static MappingFactory instance;
    private Map<String, KeyMapper> keyMapper;
    private Map<String, ValueMapper> valueMapper;
    private List<String> mapperFileLocations;
    private Map<String, String> functionClassAliases;


    private MappingFactory() {
        if (instance == null) {
            instance = this;
            instance.keyMapper = new HashMap<>();
            instance.valueMapper = new HashMap<>();
            instance.functionClassAliases = new HashMap<>();
        }
    }

    public static MappingFactory access() {
        if (instance == null) {
            new MappingFactory();
        }
        return instance;
    }

    public void init() throws IOException {
        StatementParser parser = new StatementParser();
        for (String fileLocation : mapperFileLocations) {
            List<Map<String, Mapper>> mappers = parser.parse(fileLocation);
            for (Map<String, Mapper> keyAndMapper : mappers) {
                for (String id : keyAndMapper.keySet()) {
                    Mapper mapper = keyAndMapper.get(id);
                    if (mapper instanceof KeyMapper) {
                        keyMapper.put(id, (KeyMapper) mapper);
                    } else if (mapper instanceof ValueMapper) {
                        valueMapper.put(id, (ValueMapper) mapper);
                    }
                }
            }
        }
    }

    public void setMapperFileLocations(List<String> mapperFileLocations) {
        this.mapperFileLocations = mapperFileLocations;
    }

    public List<String> getMapperFileLocations() {
        return mapperFileLocations;
    }

    public void setFunctionClassAliases(Map<String, String> functionClassAliases) {
        this.functionClassAliases = functionClassAliases;
    }

    public Map<String, String> getFunctionClassAliases() {
        return functionClassAliases;
    }

    public String getFunctionClassByAlias(String alias) {
        return functionClassAliases.get(alias);
    }

}
