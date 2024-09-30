package mb.dnm.dispatcher.http;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.http.HttpAPITemplate;
import mb.dnm.access.http.HttpServletRequestEntity;
import mb.dnm.core.ServiceProcessor;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;
import mb.dnm.util.GZipUtils;
import mb.dnm.util.MessageUtil;
import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * HTTP request 처리하는 handler 이다.
 * HttpServletRequest 의 요청 URL 및 HTTP Method 와 맵핑되는 InterfaceInfo 가  <code>StorageManager</code>에 존재하는 지 확인한 후
 * <code>ServiceProcess</code> 의 process 를 수행한다.<br><br>
 *
 * URL이 존재하지 않는 경우 404 응답을, URL은 존재하지만 요청 메소드가 유효하지 않은 경우 405 를 응답한다.<br><br>
 *
 * <code>ServiceProcess</code>의 process 메소드를 호출하여 서비스 프로세스를 수행하기 전, <code>ServiceContext</code> 객체에 input으로
 * Http headers, Http parameters, Http body를 전달하며, 수행되는 각 서비스에서
 * HTTP_HEADERS, HTTP_PARAMETERS, HTTP_BODY 으로 접근가능하다.<br><br>
 *
 * @author Yuhyun O
 * @version 2024.09.27
 *
 * */
@Slf4j
public class HttpRequestDispatcher extends HttpServlet {
    /**
     * HTTP 요청 헤더에 대한 정보이다.<br>
     * $http_headers 라는 이름으로 <code>ServiceContext</code>에 input 된다.
     * 
     * @Type <code>Map&lt;String, String&gt;</code>
     * */
    public static final String HTTP_HEADERS = "$http_headers";
    /**
     * HTTP 요청 파라미터 대한 정보이다.<br>
     * $http_parameters 라는 이름으로 <code>ServiceContext</code>에 input 된다.
     * 
     * @Type <code>Map&lt;String, String&gt;</code>
     * */
    public static final String HTTP_PARAMETERS = "$http_parameters";
    /**
     * HTTP 요청 바디에 대한 정보이다.<br>
     * $http_body 라는 이름으로 <code>ServiceContext</code>에 input 된다.
     * 
     * @Type <code>byte[]</code>
     * */
    public static final String HTTP_BODY = "$http_body";
    /**
     * HttpServletResponse 객체이다.<br>
     * $http_servlet_response 라는 이름으로 <code>ServiceContext</code>에 input 된다.
     * 
     * @Type <code>javax.servlet.http.HttpServletResponse</code>
     * */
    public static final String SERVLET_RESPONSE = "$http_servlet_response";
    public static final String RESPONSE_BODY = "$http_response_body";

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getMethod();
        if (method.equals("PATCH")) {
            doDispatch(request, response);
            
        } else {
            super.service(request, response);
        }
    }

    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispatch(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispatch(request, response);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispatch(request, response);
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispatch(request, response);
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doOptions(request, response);
    }

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getRequestURI();
        String method = request.getMethod();
        InterfaceInfo info = StorageManager.access().getInterfaceInfoOfHttpRequest(url, method);
        HttpAPITemplate apiTemplate = null;

        try {
            if (info == null) {
                writeServiceNotFound(request, response);
                return;
            }

            ServiceContext ctx = new ServiceContext(info);
            apiTemplate = info.getHttpAPITemplate();

            if (!info.isPermittedHttpMethod(method)) {
                writeMethodNotAllowed(apiTemplate, request, response);
                return;
            }

            HttpServletRequestEntity requestEntity = new HttpServletRequestEntity(request);

            ctx.addContextParam(HTTP_HEADERS, requestEntity.getHeaders());
            ctx.addContextParam(HTTP_PARAMETERS, requestEntity.getParameters());
            ctx.addContextParam(HTTP_BODY, requestEntity.getByteArrayBody());
            ctx.addContextParam(SERVLET_RESPONSE, response);

            String txId = ctx.getTxId();
            log.info("[{}]A new interface transaction was created", txId);
            ServiceProcessor.unfoldServices(ctx);
            log.info("[{}]The interface transaction was ended", txId);

            Object responseBody = ctx.getContextParam(RESPONSE_BODY);
            writeResponse(responseBody, apiTemplate, request, response);

        } catch (Exception e) {
            try {
                writeInternalServerError(apiTemplate, request, response);
            }catch (Exception ie) {
                log.error("Response failed", ie);
            }
            log.error("HttpRequestHandling failed", e);
        }

    }

    private int writeServiceNotFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> outData = new HashMap<>();
        Map<String, Object> inData = new HashMap<>();
        inData.put("status", HttpServletResponse.SC_NOT_FOUND);
        inData.put("message", "Service Not found");
        outData.put("data", inData);

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);

        return writeResponse(outData, null, request, response);
    }

    private int writeMethodNotAllowed(HttpAPITemplate apiTemplate, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> outData = new HashMap<>();
        Map<String, Object> inData = new HashMap<>();
        inData.put("status", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        inData.put("message", "Method Not Allowed");
        outData.put("data", inData);

        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

        return writeResponse(outData, apiTemplate, request, response);
    }

    private int writeInternalServerError(HttpAPITemplate apiTemplate, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> outData = new HashMap<>();
        Map<String, Object> inData = new HashMap<>();
        inData.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        inData.put("message", "Internal Server Error");
        outData.put("data", inData);

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        return writeResponse(outData, apiTemplate, request, response);
    }

    private int writeResponse(Object body, HttpAPITemplate apiTemplate, HttpServletRequest request, HttpServletResponse response) throws Exception {
        int len = 0;
        String accept =  request.getHeader("accept");
        String contentType = null;

        if (apiTemplate != null) {

            for (Map.Entry<String, String> entry : apiTemplate.getResponseHeaders().entrySet()) {
                response.setHeader(entry.getKey(), entry.getValue());
            }

            //accept 의 content-type 이 HttpAPITemplate 에 존재하면 해당 api 타입으로 응답함
            if (accept != null && apiTemplate.getContentTypes().contains(accept)) {
                contentType = accept;
            } else {
                // 없다면 HttpAPITemplate에 지정된 type 중 가장 첫번째 값으로 응답함
                contentType = apiTemplate.getPreferentialContentType();
            }

            response.setContentType(contentType);

        } else {
            if (accept != null) {
                contentType = accept;
            }
        }

        System.out.println("@@@ " + contentType);
        if (body != null) {
            byte[] byteBody = null;

            if (contentType != null) {
                if (contentType.toLowerCase().contains("json")) {
                    if (body instanceof byte[]) {
                        byteBody = (byte[]) body;
                    } else if (body instanceof Map) {
                        byteBody = MessageUtil.mapToJson((Map) body, false).getBytes();
                    } else if (body instanceof String) {
                        byteBody = ((String) body).getBytes();
                    }
                    response.setContentType("application/json");

                } else if (contentType.toLowerCase().contains("xml")) {
                    if (body instanceof byte[]) {
                        byteBody = (byte[]) body;
                    } else if (body instanceof Map) {
                        byteBody = MessageUtil.mapToXml((Map) body, false).getBytes();
                    } else if (body instanceof String) {
                        byteBody = ((String) body).getBytes();
                    }
                    response.setContentType("application/xml");

                } else {
                    if (body instanceof byte[]) {
                        byteBody = (byte[]) body;
                    } else if (body instanceof String) {
                        byteBody = ((String) body).getBytes();
                    } else if (body instanceof Map) {
                        byteBody = MessageUtil.mapToJson((Map) body, false).getBytes();
                        response.setContentType("application/json");
                    }
                }
            } else {
                if (body instanceof byte[]) {
                    byteBody = (byte[]) body;
                    response.setContentType("text/plain");

                } else if (body instanceof String) {
                    byteBody = ((String) body).getBytes();
                    response.setContentType("text/plain");

                } else if (body instanceof Map) {
                    byteBody = MessageUtil.mapToJson((Map) body, false).getBytes();
                    response.setContentType("application/json");
                }
            }

            response.setHeader("Server", "Indigo-API");

            try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
                bos.write(byteBody);
                bos.flush();
            }
        }

        return 0;
    }

}
