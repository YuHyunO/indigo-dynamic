package mb.dnm.access.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpResponseEntity implements Serializable {
    private static final long serialVersionUID = -7186121775917736576L;
    private byte[] body;
    @Getter
    private Map<String, String> headers;
    @Getter
    private int responseCode;
    private final int BUFFER_SIZE = 64 * 1024;

    public HttpResponseEntity(HttpURLConnection connection) throws IOException {
        this.responseCode = connection.getResponseCode();
        this.body = getResponseBody(connection);
        this.headers = getResponseHeaders(connection);
    }

    private byte[] getResponseBody(HttpURLConnection connection) throws IOException {
        byte[] bodyData = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream bas = new ByteArrayOutputStream();

        try {
            InputStream is = null;
            if (this.responseCode < 400) {
                is = connection.getInputStream();
            } else {
                is = connection.getErrorStream();
            }
            bis = new BufferedInputStream(is);
            byte[] buffer = new byte[BUFFER_SIZE];
            bas = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(bas);

            int i = 0;
            while((i = bis.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, i);
                bos.flush();
            }
            bodyData = bas.toByteArray();

        }catch (IOException ie) {
            throw ie;
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (bas != null) {
                bas.close();
            }
        }
        if (bodyData != null) {
            log.debug("HttpResponseEntity.getResponseBody()-Read body length:{}", bodyData.length);
        } else {
            log.debug("HttpResponseEntity.getResponseBody()-Read body is null");
        }
        return bodyData;
    }

    public byte[] getByteArrayBody() {
        return this.body;
    }

    public String getStringBody() {
        if (body == null) {
            return null;
        }
        return new String(body);
    }

    public String getStringBody(String charSet) throws UnsupportedEncodingException {
        if (body == null) {
            return null;
        }
        return new String(body, charSet);
    }

    public Map<String, Object> getJsonBody() throws UnsupportedEncodingException, JsonProcessingException {
        if (body == null) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<LinkedHashMap<String, Object>> typeReference = new TypeReference<LinkedHashMap<String,Object>>() {};
        return objectMapper.readValue(getStringBody(), typeReference);
    }

    public Map<String, Object> getJsonBody(String charSet) throws UnsupportedEncodingException, JsonProcessingException {
        if (body == null) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<LinkedHashMap<String, Object>> typeReference = new TypeReference<LinkedHashMap<String,Object>>() {};
        return objectMapper.readValue(getStringBody(charSet), typeReference);
    }

    private Map<String, String> getResponseHeaders(HttpURLConnection connection) {
        Map<String, String> responseHeaders = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            for (String value : header.getValue()) {
                String name = header.getKey();
                if (name != null) {
                    responseHeaders.put(header.getKey(), value);
                }
            }
        }
        return responseHeaders;
    }

}
