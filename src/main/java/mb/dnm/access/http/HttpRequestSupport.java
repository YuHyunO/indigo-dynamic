package mb.dnm.access.http;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.util.MessageUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class HttpRequestSupport {

    private int connectTimeout = 10000;
    private int readTimeout = 10000;
    private boolean useCashes = false;
    private Map<String, String> headers;
    private Map<String, String[]> parameters;
    private List<String> pathParameters;
    private Map<String, Object> formDataParams;
    private boolean clearHeadersAfterSend = true;
    private boolean clearParamsAfterSend = true;

    public HttpRequestSupport() {
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
        this.pathParameters = new ArrayList<>();
        this.formDataParams = new HashMap<>();
    }

    public HttpRequestSupport(int connectTimeout, int readTimeout, boolean useCashes) {
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
        this.pathParameters = new ArrayList<>();
        this.formDataParams = new HashMap<>();
        setConnectTimeout(connectTimeout);
        setReadTimeout(readTimeout);
        this.useCashes = useCashes;
    }

    public HttpResponseEntity getRequest(String url) throws MalformedURLException, IOException {
        return request(url, "GET", null, null, null);
    }

    public HttpResponseEntity getRequest(String url, Map<String, String[]> parameters) throws MalformedURLException, IOException {
        return request(url, "GET", parameters, null, null);
    }

    public HttpResponseEntity postRequest(String url) throws MalformedURLException, IOException {
        return request(url, "POST", null, null, null);
    }

    public HttpResponseEntity postRequest(String url, byte[] body) throws MalformedURLException, IOException {
        return request(url, "POST", null, null, body);
    }

    public HttpResponseEntity postRequest(String url, Object body) throws MalformedURLException, IOException {
        if (body instanceof String) {
            return postRequest(url, (String) body);
        } else if (body instanceof Map) {
            return postRequest(url, (Map<String, Object>) body);
        } else if (body instanceof byte[]) {
            return postRequest(url, (byte[]) body);
        } else {
            throw new IllegalArgumentException("Unsupported http request body type. '" + body.getClass().getName() + "'");
        }
    }

    public HttpResponseEntity postRequest(String url, String body) throws MalformedURLException, IOException {
        return request(url, "POST", null, null, body.getBytes());
    }

    public HttpResponseEntity postUrlEncodedFormData(String url) throws IOException {
        this.headers.put("Content-Type", "application/x-www-form-urlencoded");
        return request(url, "POST", null, headers, getParamsByte(this.formDataParams));
    }

    public HttpResponseEntity postUrlEncodedFormData(String url, Map<String, Object> formDataParams) throws IOException {
        this.headers.put("Content-Type", "application/x-www-form-urlencoded");
        return request(url, "POST", null, headers, getParamsByte(formDataParams));
    }

    public HttpResponseEntity postUrlEncodedFormData(String url, Map<String, String[]> parameters, Map<String, String> headers) throws IOException {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return request(url, "POST", parameters, headers, getParamsByte(this.formDataParams));
    }

    public HttpResponseEntity postRequest(String url, Map<String, Object> body) throws MalformedURLException, IOException {
        String jsonData = MessageUtil.mapToJson(body, false);
        return request(url, "POST", null, null, jsonData.getBytes());
    }

    public HttpResponseEntity request(String url, String method, Map<String, String[]> parameters, Map<String, String> headers, byte[] body) throws IOException {
        if (!pathParameters.isEmpty()) {
            url = getPathParamMappedUrl(url, this.pathParameters);
        }
        if (parameters != null) {
            this.parameters.putAll(parameters);
        }
        if (headers != null) {
            this.headers.putAll(headers);
        }
        URL paramMappedUrl = new URL(getParamMappedURL(url, this.parameters));
        HttpURLConnection connection = (HttpURLConnection) paramMappedUrl.openConnection();
        connection.setRequestMethod(method);
        setRequestHeaders(connection, this.headers);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(this.connectTimeout);
        connection.setReadTimeout(this.readTimeout);

        if (body != null) {
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(connection.getOutputStream());
                bos.write(body);
                bos.flush();

            } catch (IOException ie) {
                throw ie;
            } finally {
                if (bos != null) bos.close();
            }
            log.debug("Writing request (body length:{})", body.length);
        } else {
            log.debug("Connecting ... (body length:0)");
            connection.connect();
        }

        if (clearHeadersAfterSend) {
            this.headers = new HashMap<>();
        }
        if (clearParamsAfterSend) {
            this.parameters = new HashMap<>();
        }
        if (clearParamsAfterSend) {
            this.pathParameters = new ArrayList<>();
        }
        return new HttpResponseEntity(connection);
    }

    public void setConnectTimeout(int connectTimeout) {
        if (connectTimeout < 0) throw new IllegalArgumentException("The property 'connectTimeout' can't be lesser than 0");
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        if (readTimeout < 0) throw new IllegalArgumentException("The property 'readTimeout' can't be lesser than 0");
        this.readTimeout = readTimeout;
    }

    protected String getPathParamMappedUrl(String url, List<String> pathParameters) throws UnsupportedEncodingException {
        if (url == null) {
            throw new IllegalArgumentException("The argument 'url' is null");
        }
        url =deleteUrlSlash(url);
        StringBuilder mappedUrl = new StringBuilder(url);
        if (pathParameters != null && !pathParameters.isEmpty()) {

            for (String param : pathParameters) {
                mappedUrl.append("/" + param);
            }
        }
        return mappedUrl.toString();
    }

    protected String getParamMappedURL(String url, Map<String, String[]> parameters) throws UnsupportedEncodingException {
        if (url == null) {
            throw new IllegalArgumentException("The argument 'url' is null");
        }
        if (parameters != null && !parameters.isEmpty()) {
            StringBuilder paramMappedURL = null;
            url = deleteUrlSlash(url);
            if (url.endsWith("?")) {
                paramMappedURL = new StringBuilder(url);
            } else {
                paramMappedURL = new StringBuilder(url + "?");
            }
            for (String paramName : parameters.keySet()) {

                for (String value : parameters.get(paramName)) {
                    paramMappedURL.append(encodeParam(paramName) + "=" + encodeParam(value) + "&");
                }
            }
            paramMappedURL.setLength(paramMappedURL.length() - 1);
            return paramMappedURL.toString();
        }
        return url;
    }

    private String deleteUrlSlash(String url) {
        while (true) {
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            } else {
                return url;
            }
        }
    }

    protected void setRequestHeaders(HttpURLConnection connection, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            for (String headerName : headers.keySet()) {
                String value = headers.get(headerName);
                connection.setRequestProperty(headerName, value);
            }
        }
    }

    private byte[] getParamsByte(Map<String, Object> formDataParams) throws UnsupportedEncodingException {
        byte[] result = null;
        StringBuilder postData = new StringBuilder();

        if (formDataParams != null) {
            this.formDataParams.putAll(formDataParams);
        }

        for (String key : this.formDataParams.keySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(this.encodeParam(key));
            postData.append('=');
            postData.append(this.encodeParam(String.valueOf(formDataParams.get(key))));
        }

        try {
            result = postData.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw e;
        }
        return result;
    }

    private String encodeParam(String data) throws  UnsupportedEncodingException {
        String result = "";
        try {
            result = URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw e;
        }
        return result;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
    public void addHeader(Map<String, String> headers) {
        for (String paramKey : headers.keySet()) {
            this.headers.put(paramKey, headers.get(paramKey));
        }
    }

    public void addParameter(String key, String value) {
        this.parameters.put(key, new String[]{value});
    }

    public void addParameter(String key, boolean flag) {
        this.parameters.put(key, new String[]{String.valueOf(flag)});
    }

    public void addParameter(String key, int value) {
        this.parameters.put(key, new String[]{String.valueOf(value)});
    }

    public void addPathParameter(String value) {
        while (true) {
            if (value.startsWith("/")) {
                value = value.substring(1);
            } else if (value.endsWith("/")) {
                value = value.substring(0, value.length() - 1);
            } else {
                break;
            }
        }
        this.pathParameters.add(value);
    }

    public void addPathParameter(int index, String value) {
        while (true) {
            if (value.startsWith("/")) {
                value = value.substring(1);
            } else if (value.endsWith("/")) {
                value = value.substring(0, value.length() - 1);
            } else {
                break;
            }
        }
        int size = this.pathParameters.size();
        if (index >= size) {
            this.pathParameters.add(value);
        } else if (index <= -1) {
            this.pathParameters.add(0, value);
        } else {
            this.pathParameters.add(index, value);
        }
    }

    public void addFormDataParam(String key, String value) {
        this.formDataParams.put(key, value);
    }

    public void setClearHeadersAfterSend(boolean clearParamsAfterSend) {
        this.clearParamsAfterSend = clearParamsAfterSend;
    }

    public void setClearParamsAfterSend(boolean clearParamsAfterSend) {
        this.clearParamsAfterSend = clearParamsAfterSend;
    }


}
