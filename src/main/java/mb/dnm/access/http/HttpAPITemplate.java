package mb.dnm.access.http;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.code.HttpMethod;

import java.io.Serializable;
import java.util.*;

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

    public HttpAPITemplate() {
        contentTypes = new ArrayList<>();
        responseHeaders = new HashMap<>();
    }

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

    public boolean isPermittedFrontMethod(String frontMethod) {
        if (frontMethods != null) {
            return frontMethods.contains(HttpMethod.valueOf(frontMethod.toUpperCase()));
        }
        return false;
    }

    public void setContentTypes(String contentTypes) {
        String[] contentTypesArr = contentTypes.trim().split(",");
        for (String contentType : contentTypesArr) {
            String type = contentType.toLowerCase().trim();
            if (!this.contentTypes.contains(type)) {
                this.contentTypes.add(contentType.trim().toLowerCase());
            }
        }
    }

    public String getPreferentialContentType() {
        if (!contentTypes.isEmpty()) {
            return contentTypes.get(0);
        }
        return "text/plain";
    }

    public void setContentEncoding(String contentEncoding) {
        contentEncoding = contentEncoding.trim();
        if (contentEncoding.equalsIgnoreCase("gzip")) {
            this.contentEncoding = "gzip";
        }
    }

}
