package mb.dnm.access.http;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.code.HttpMethod;

import java.io.Serializable;
import java.util.*;

/**
 * The type Http api template.
 */
@Setter
@Getter
public class HttpAPITemplate implements Serializable {
    private static final long serialVersionUID = -7889211422659141612L;
    private String frontUrl;
    private Set<HttpMethod> frontMethods;
    private String backendUrl;
    private String backendMethod;

    private String accept ="*";
    private String acceptEncoding;
    private List<String> contentTypes;
    private String contentEncoding;
    private Map<String, String> responseHeaders;

    /**
     * Instantiates a new Http api template.
     */
    public HttpAPITemplate() {
        contentTypes = new ArrayList<>();
        responseHeaders = new HashMap<>();
    }

    /**
     * Sets front methods.
     *
     * @param frontMethods the front methods
     */
    public void setFrontMethods(String frontMethods) {
        if (this.frontMethods == null) {
            this.frontMethods = new HashSet<>();
        }

        frontMethods = frontMethods.replace(" ", "");
        if (frontMethods.isEmpty()) {
            throw new IllegalArgumentException("The frontHttpMethod is empty");
        }

        String[] methodArr = frontMethods.split(",");
        for (String method : methodArr) {
            switch (method.toUpperCase()) {
                case "GET": this.frontMethods.add(HttpMethod.GET); break;
                case "POST": this.frontMethods.add(HttpMethod.POST); break;
                case "PUT": this.frontMethods.add(HttpMethod.PUT); break;
                case "DELETE": this.frontMethods.add(HttpMethod.DELETE); break;
                case "*": {
                    this.frontMethods.add(HttpMethod.GET);
                    this.frontMethods.add(HttpMethod.POST);
                    this.frontMethods.add(HttpMethod.PUT);
                    this.frontMethods.add(HttpMethod.DELETE);
                    break;
                }
                default: throw new IllegalArgumentException("The http method " + method + " is not supported");
            }
        }
    }

    /**
     * Is permitted front method boolean.
     *
     * @param frontMethod the front method
     * @return the boolean
     */
    public boolean isPermittedFrontMethod(String frontMethod) {
        if (frontMethods != null) {
            return frontMethods.contains(HttpMethod.valueOf(frontMethod.toUpperCase()));
        }
        return false;
    }

    /**
     * Sets content types.
     *
     * @param contentTypes the content types
     */
    public void setContentTypes(String contentTypes) {
        String[] contentTypesArr = contentTypes.trim().split(",");
        for (String contentType : contentTypesArr) {
            String type = contentType.toLowerCase().trim();
            if (!this.contentTypes.contains(type)) {
                this.contentTypes.add(contentType.trim().toLowerCase());
            }
        }
    }

    /**
     * Gets preferential content type.
     *
     * @return the preferential content type
     */
    public String getPreferentialContentType() {
        if (!contentTypes.isEmpty()) {
            return contentTypes.get(0);
        }
        return "text/plain";
    }

    /**
     * Sets content encoding.
     *
     * @param contentEncoding the content encoding
     */
    public void setContentEncoding(String contentEncoding) {
        contentEncoding = contentEncoding.trim();
        if (contentEncoding.equalsIgnoreCase("gzip")) {
            this.contentEncoding = "gzip";
        }
    }

    /**
     * Gets front url.
     *
     * @return the front url
     */
    public String getFrontUrl() {
        return frontUrl;
    }

    /**
     * Sets front url.
     *
     * @param frontUrl the front url
     */
    public void setFrontUrl(String frontUrl) {
        this.frontUrl = frontUrl;
    }

    /**
     * Gets front methods.
     *
     * @return the front methods
     */
    public Set<HttpMethod> getFrontMethods() {
        return frontMethods;
    }

    /**
     * Sets front methods.
     *
     * @param frontMethods the front methods
     */
    public void setFrontMethods(Set<HttpMethod> frontMethods) {
        this.frontMethods = frontMethods;
    }

    /**
     * Gets backend url.
     *
     * @return the backend url
     */
    public String getBackendUrl() {
        return backendUrl;
    }

    /**
     * Sets backend url.
     *
     * @param backendUrl the backend url
     */
    public void setBackendUrl(String backendUrl) {
        this.backendUrl = backendUrl;
    }

    /**
     * Gets backend method.
     *
     * @return the backend method
     */
    public String getBackendMethod() {
        return backendMethod;
    }

    /**
     * Sets backend method.
     *
     * @param backendMethod the backend method
     */
    public void setBackendMethod(String backendMethod) {
        this.backendMethod = backendMethod;
    }

    /**
     * Gets accept.
     *
     * @return the accept
     */
    public String getAccept() {
        return accept;
    }

    /**
     * Sets accept.
     *
     * @param accept the accept
     */
    public void setAccept(String accept) {
        this.accept = accept;
    }

    /**
     * Gets accept encoding.
     *
     * @return the accept encoding
     */
    public String getAcceptEncoding() {
        return acceptEncoding;
    }

    /**
     * Sets accept encoding.
     *
     * @param acceptEncoding the accept encoding
     */
    public void setAcceptEncoding(String acceptEncoding) {
        this.acceptEncoding = acceptEncoding;
    }

    /**
     * Gets content types.
     *
     * @return the content types
     */
    public List<String> getContentTypes() {
        return contentTypes;
    }

    /**
     * Sets content types.
     *
     * @param contentTypes the content types
     */
    public void setContentTypes(List<String> contentTypes) {
        this.contentTypes = contentTypes;
    }

    /**
     * Gets content encoding.
     *
     * @return the content encoding
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Gets response headers.
     *
     * @return the response headers
     */
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Sets response headers.
     *
     * @param responseHeaders the response headers
     */
    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
}
