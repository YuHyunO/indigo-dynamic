package mb.dnm.access.http;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.util.MessageUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The type Http request support.
 */
@Slf4j
public class HttpRequestSupport implements Serializable {

    private static final long serialVersionUID = -7681787300349640386L;
    private int connectTimeout = 10000;
    private int readTimeout = 10000;
    private boolean useCashes = false;
    private Map<String, String> headers;
    private Map<String, String[]> parameters;
    private List<String> pathParameters;
    private Map<String, Object> formDataParams;
    private boolean clearHeadersAfterSend = true;
    private boolean clearParamsAfterSend = true;

    /**
     * Instantiates a new Http request support.
     */
    public HttpRequestSupport() {
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
        this.pathParameters = new ArrayList<>();
        this.formDataParams = new HashMap<>();
    }

    /**
     * Instantiates a new Http request support.
     *
     * @param connectTimeout the connect timeout
     * @param readTimeout    the read timeout
     * @param useCashes      the use cashes
     */
    public HttpRequestSupport(int connectTimeout, int readTimeout, boolean useCashes) {
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
        this.pathParameters = new ArrayList<>();
        this.formDataParams = new HashMap<>();
        setConnectTimeout(connectTimeout);
        setReadTimeout(readTimeout);
        this.useCashes = useCashes;
    }

    /**
     * Gets request.
     *
     * @param url the url
     * @return the request
     * @throws MalformedURLException the malformed url exception
     * @throws IOException           the io exception
     */
    public HttpResponseEntity getRequest(String url) throws MalformedURLException, IOException {
        return request(url, "GET", null, null, null);
    }

    /**
     * Gets request.
     *
     * @param url        the url
     * @param parameters the parameters
     * @return the request
     * @throws MalformedURLException the malformed url exception
     * @throws IOException           the io exception
     */
    public HttpResponseEntity getRequest(String url, Map<String, String[]> parameters) throws MalformedURLException, IOException {
        return request(url, "GET", parameters, null, null);
    }

    /**
     * Post request http response entity.
     *
     * @param url the url
     * @return the http response entity
     * @throws MalformedURLException the malformed url exception
     * @throws IOException           the io exception
     */
    public HttpResponseEntity postRequest(String url) throws MalformedURLException, IOException {
        return request(url, "POST", null, null, null);
    }

    /**
     * Post request http response entity.
     *
     * @param url  the url
     * @param body the body
     * @return the http response entity
     * @throws MalformedURLException the malformed url exception
     * @throws IOException           the io exception
     */
    public HttpResponseEntity postRequest(String url, byte[] body) throws MalformedURLException, IOException {
        return request(url, "POST", null, null, body);
    }

    /**
     * Post request http response entity.
     *
     * @param url  the url
     * @param body the body
     * @return the http response entity
     * @throws MalformedURLException the malformed url exception
     * @throws IOException           the io exception
     */
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

    /**
     * Post request http response entity.
     *
     * @param url  the url
     * @param body the body
     * @return the http response entity
     * @throws MalformedURLException the malformed url exception
     * @throws IOException           the io exception
     */
    public HttpResponseEntity postRequest(String url, String body) throws MalformedURLException, IOException {
        return request(url, "POST", null, null, body.getBytes());
    }

    /**
     * Post url encoded form data http response entity.
     *
     * @param url the url
     * @return the http response entity
     * @throws IOException the io exception
     */
    public HttpResponseEntity postUrlEncodedFormData(String url) throws IOException {
        this.headers.put("Content-Type", "application/x-www-form-urlencoded");
        return request(url, "POST", null, headers, getParamsByte(this.formDataParams));
    }

    /**
     * Post url encoded form data http response entity.
     *
     * @param url            the url
     * @param formDataParams the form data params
     * @return the http response entity
     * @throws IOException the io exception
     */
    public HttpResponseEntity postUrlEncodedFormData(String url, Map<String, Object> formDataParams) throws IOException {
        this.headers.put("Content-Type", "application/x-www-form-urlencoded");
        return request(url, "POST", null, headers, getParamsByte(formDataParams));
    }

    /**
     * Post url encoded form data http response entity.
     *
     * @param url        the url
     * @param parameters the parameters
     * @param headers    the headers
     * @return the http response entity
     * @throws IOException the io exception
     */
    public HttpResponseEntity postUrlEncodedFormData(String url, Map<String, String[]> parameters, Map<String, String> headers) throws IOException {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return request(url, "POST", parameters, headers, getParamsByte(this.formDataParams));
    }

    /**
     * Post request http response entity.
     *
     * @param url  the url
     * @param body the body
     * @return the http response entity
     * @throws MalformedURLException the malformed url exception
     * @throws IOException           the io exception
     */
    public HttpResponseEntity postRequest(String url, Map<String, Object> body) throws MalformedURLException, IOException {
        String jsonData = MessageUtil.mapToJson(body, false);
        return request(url, "POST", null, null, jsonData.getBytes());
    }

    /**
     * Request http response entity.
     *
     * @param url        the url
     * @param method     the method
     * @param parameters the parameters
     * @param headers    the headers
     * @param body       the body
     * @return the http response entity
     * @throws IOException the io exception
     */
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

    /**
     * Sets connect timeout.
     *
     * @param connectTimeout the connect timeout
     */
    public void setConnectTimeout(int connectTimeout) {
        if (connectTimeout < 0) throw new IllegalArgumentException("The property 'connectTimeout' can't be lesser than 0");
        this.connectTimeout = connectTimeout;
    }

    /**
     * Sets read timeout.
     *
     * @param readTimeout the read timeout
     */
    public void setReadTimeout(int readTimeout) {
        if (readTimeout < 0) throw new IllegalArgumentException("The property 'readTimeout' can't be lesser than 0");
        this.readTimeout = readTimeout;
    }

    /**
     * Gets path param mapped url.
     *
     * @param url            the url
     * @param pathParameters the path parameters
     * @return the path param mapped url
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
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

    /**
     * Gets param mapped url.
     *
     * @param url        the url
     * @param parameters the parameters
     * @return the param mapped url
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
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

    /**
     * Sets request headers.
     *
     * @param connection the connection
     * @param headers    the headers
     */
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

    /**
     * Add header.
     *
     * @param key   the key
     * @param value the value
     */
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    /**
     * Add header.
     *
     * @param headers the headers
     */
    public void addHeader(Map<String, String> headers) {
        for (String paramKey : headers.keySet()) {
            this.headers.put(paramKey, headers.get(paramKey));
        }
    }

    /**
     * Add parameter.
     *
     * @param key   the key
     * @param value the value
     */
    public void addParameter(String key, String value) {
        this.parameters.put(key, new String[]{value});
    }

    /**
     * Add parameter.
     *
     * @param key  the key
     * @param flag the flag
     */
    public void addParameter(String key, boolean flag) {
        this.parameters.put(key, new String[]{String.valueOf(flag)});
    }

    /**
     * Add parameter.
     *
     * @param key   the key
     * @param value the value
     */
    public void addParameter(String key, int value) {
        this.parameters.put(key, new String[]{String.valueOf(value)});
    }

    /**
     * Add path parameter.
     *
     * @param value the value
     */
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

    /**
     * Add path parameter.
     *
     * @param index the index
     * @param value the value
     */
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

    /**
     * Add form data param.
     *
     * @param key   the key
     * @param value the value
     */
    public void addFormDataParam(String key, String value) {
        this.formDataParams.put(key, value);
    }

    /**
     * Sets clear headers after send.
     *
     * @param clearParamsAfterSend the clear params after send
     */
    public void setClearHeadersAfterSend(boolean clearParamsAfterSend) {
        this.clearParamsAfterSend = clearParamsAfterSend;
    }

    /**
     * Sets clear params after send.
     *
     * @param clearParamsAfterSend the clear params after send
     */
    public void setClearParamsAfterSend(boolean clearParamsAfterSend) {
        this.clearParamsAfterSend = clearParamsAfterSend;
    }


}
