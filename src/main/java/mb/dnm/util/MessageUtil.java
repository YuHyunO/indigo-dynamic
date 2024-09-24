package mb.dnm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MessageUtil {
    private MessageUtil() {}

    public static StringBuffer toStringBuf(Throwable throwable) {
        if (throwable == null)
            return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.getBuffer();
    }

    public static String toString(Throwable throwable) {
        return toStringBuf(throwable).toString();
    }

    public static String mapToJson(Map map, boolean prettyFormat) throws JsonProcessingException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return mapToJson(map, prettyFormat, dateFormat);
    }

    public static String mapToJson(Map map, boolean prettyFormat, SimpleDateFormat dateFormat) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(dateFormat);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        String jsonString = objectMapper.writeValueAsString(map);

        if (prettyFormat) {
            objectMapper = new ObjectMapper();
            Object jsonObject = objectMapper.readValue(jsonString, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        }
        return jsonString;
    }

    public static Map<String, Object> jsonToMap(String jsonData) throws JsonProcessingException{
        if (jsonData == null)
            return null;
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String,Object>>() {};
        return objectMapper.readValue(jsonData, typeReference);
    }

    public static String mapToXml (Map map, boolean prettyFormat) throws JsonProcessingException {
        return mapToXml(map, null, prettyFormat);
    }

    public static String mapToXml (Map map, String rootName, boolean prettyFormat) throws JsonProcessingException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return mapToXml(map, rootName, prettyFormat, dateFormat);
    }

    public static String mapToXml (Map map, String rootName, boolean prettyFormat, SimpleDateFormat dateFormat) throws JsonProcessingException {
        XmlMapper xmlMapper = XmlMapper.builder().build();
        xmlMapper.setDateFormat(dateFormat);
        xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        if (prettyFormat) {
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        if (rootName == null || rootName.isEmpty()) {
            List<Object> keyList = new ArrayList<>(map.keySet());
            String root = "data";
            if (keyList.size() == 1) {
                Object key = keyList.get(0);
                Object value = map.get(key);
                if (key instanceof String) {
                    root = String.valueOf(key);
                }
                return xmlMapper.writer().withRootName(root).writeValueAsString(value);
            }
            return xmlMapper.writer().withRootName(root).writeValueAsString(map);
        } else {
            return xmlMapper.writer().withRootName(rootName).writeValueAsString(map);
        }
    }
}
