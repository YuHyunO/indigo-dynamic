package mb.dnm.access.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
public class HttpServletRequestEntity implements Serializable {
    private static final long serialVersionUID = 2088174028218642348L;
    private HttpServletRequest request;
    private byte[] body;
    @Getter
    private Map<String, String> headers;
    @Getter
    private Map<String, String> parameters;
    @Getter
    private String method;
    @Getter
    private String requestURI;
    @Getter
    private String remoteHost;
    @Getter
    private int remotePort;
    @Getter
    private String contentType;
    @Getter
    private String contentEncoding;
    @Getter
    private String protocol;
    @Getter
    private int contentLength;
    private final int BUFFER_SIZE = 8 * 1024;

    public HttpServletRequestEntity(HttpServletRequest request) throws IOException {
        this.request = request;
        this.body = getRequestBody();
        this.headers = getRequestHeaders();
        this.parameters = getRequestParams();
        this.method = request.getMethod();
        this.requestURI = request.getRequestURI();
        this.remoteHost = request.getRemoteHost();
        this.remotePort = request.getRemotePort();
        this.contentType = request.getContentType();
        this.contentEncoding = request.getCharacterEncoding();
        this.contentLength = request.getContentLength();
        this.protocol = request.getProtocol();
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            headers.put(name.toLowerCase(), request.getHeader(name));
        }
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key.toLowerCase());
    }

    private Map<String, String> getRequestParams() {
        Map<String, String> params = new LinkedHashMap<>();
        Enumeration paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements()) {
            String name = (String) paramNames.nextElement();
            String[] values = request.getParameterValues(name);
            if (values != null && values.length > 0) {
                params.put(name, values[0]);
            }
        }
        return params;
    }

    private byte[] getRequestBody() throws IOException {
        byte[] bodyData = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream bas = null;

        try {
            InputStream is = request.getInputStream();

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
        return bodyData;
    }

    public byte[] getByteArrayBody() {
        return this.body;
    }

    public String getStringBody() throws UnsupportedEncodingException {
        return new String(body);
    }

    public String getStringBody(String charSet) throws UnsupportedEncodingException {
        return new String(body, charSet);
    }

    public Map<String, Object> getJsonBody() throws UnsupportedEncodingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String,Object>>() {};
        String bodyStr = getStringBody();
        if (bodyStr == null) {
            throw new IllegalStateException("HttpServletRequestEntity.getJsonBody()-body is null");
        }
        return objectMapper.readValue(bodyStr, typeReference);
    }

    public Map<String, Object> getJsonBody(String charSet) throws UnsupportedEncodingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String,Object>>() {};
        return objectMapper.readValue(getStringBody(charSet), typeReference);
    }

    @Override
    public String toString() {
        return "HttpServletRequestEntity{" +
                "request=" + request +
                ", body=" + Arrays.toString(body) +
                ", headers=" + headers +
                ", parameters=" + parameters +
                ", method='" + method + '\'' +
                ", requestURI='" + requestURI + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                ", remotePort=" + remotePort +
                ", contentType='" + contentType + '\'' +
                ", contentEncoding='" + contentEncoding + '\'' +
                ", protocol='" + protocol + '\'' +
                ", contentLength=" + contentLength +
                '}';
    }
}
