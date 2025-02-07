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
 * Http request 를 수신하여 {@link ServiceProcessor} 로 dispatch 한다.<br>
 * 지원되는 HTTP Methods : {@code OPTIONS} {@code HEAD} {@code GET} {@code POST} {@code PUT} {@code DELETE} {@code PATCH}<br>
 * <br>
 * HTTP 요청 처리 순서:
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 *   1. 클라이언트로부터 특정 URL 과 HTTP Method 로 요청을 받는다.
 *   2. 요청받은 URL 과 Method 가 맵핑된 {@link InterfaceInfo} 존재하는 지 확인한다.
 *   3. 요청받은 HTTP 데이터를 아래와 같이 {@link ServiceContext} 에 저장한다.
 *     -HTTP header ->  $http_headers ({@code Map<String, String>})
 *     -HTTP parameter ->  $http_parameters ({@code Map<String, String>})
 *     -HTTP body ->  $http_body ({@code byte[]})
 *     -{@link HttpServletResponse} ->  $http_servlet_response({@code HttpServletResponse})
 *   4. {@link ServiceProcessor}로 {@code ServiceContext}를 전달한다.
 *   5. {@code ServiceProcessor}에서 service-processing 이 완료되면 {@code ServiceContext}에 저장된 HTTP Response body 를 가져와 클라이언트에게 응답한다.
 *     -HTTP Response body -> $http_response_body
 * </pre>
 * <br>
 * <br>
 * 활용예제 : DNC_HTTP_HANDLING.dnc
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 *  #namespace: IF_HTTP
 *  #import mb.dnm.dispatcher.http.HttpRequestDispatcher;
 *  #import mb.dnm.util.MessageUtil;
 *  #import javax.servlet.http.HttpServletResponse;
 *  #code_id : HANDLE_HTTP
 *  #{
 *     //1.Http header 정보 가져오기
 *     {@code Map<String, String>} headers = ctx.getContextParam(HttpRequestDispatcher.HTTP_HEADERS);
 *     //2.Http parameter 정보 가져오기
 *     {@code Map<String, String>} parameters = ctx.getContextParam(HttpRequestDispatcher.HTTP_PARAMETERS);
 *     //3.Http body 정보 가져오기
 *     {@code byte[]} byteBody = ctx.getContextParam(HttpRequestDispatcher.HTTP_BODY);
 *     //4.HttpServletResponse 가져오기
 *     {@code HttpServletResponse} response = ctx.getContextParam(HttpRequestDispatcher.SERVLET_RESPONSE);
 *
 *     //4.Http body 를 JSON 으로 형변환
 *     if (body != null) {
 *        {@code Map<String, Object>} jsonMap = MessageUtil.jsonToMap(new String(byteBody));
 *
 *         //업무로직 작성
 *
 *         {@code Map<String, Object>} responseMap = {@code new LinkedHashMap<>()}
 *          responseMap.put("result", "성공");
 *
 *          ctx.addContextParam(HttpRequestDispatcher.RESPONSE_BODY, responseMap);
 *     } else {
 *          response.setStatus(400) //Bad request
 *          return;
 *     }
 *
 *  }#
 * </pre>
 * @see HttpDispatcherServer
 */
@Slf4j
public class HttpRequestDispatcher extends HttpServlet {

    /**
     * The constant HTTP_HEADERS. ($http_headers, {@code Map<String, String>})
     */
    public static final String HTTP_HEADERS = "$http_headers";

    /**
     * The constant HTTP_PARAMETERS. ($http_parameters, {@code Map<String, String>})
     */
    public static final String HTTP_PARAMETERS = "$http_parameters";

    /**
     * The constant HTTP_BODY. ($http_body, {@code byte[]})
     */
    public static final String HTTP_BODY = "$http_body";

    /**
     * The constant SERVLET_RESPONSE. ($http_servlet_response, {@code HttpServletResponse})
     */
    public static final String SERVLET_RESPONSE = "$http_servlet_response";
    /**
     * The constant RESPONSE_BODY. ($http_response_body)
     */
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

            if (!info.isActivated()) {
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

        if (body != null) {
            byte[] byteBody = null;

            if (contentType != null) {
                if (contentType.toLowerCase().contains("json")) {
                    if (body instanceof byte[]) {
                        byteBody = (byte[]) body;
                    } else if (body instanceof Map) {
                        byteBody = MessageUtil.mapToJson((Map) body, true).getBytes();
                    } else if (body instanceof String) {
                        byteBody = ((String) body).getBytes();
                    }
                    response.setContentType("application/json");

                } else if (contentType.toLowerCase().contains("xml")) {
                    if (body instanceof byte[]) {
                        byteBody = (byte[]) body;
                    } else if (body instanceof Map) {
                        byteBody = MessageUtil.mapToXml((Map) body, true).getBytes();
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
                        byteBody = MessageUtil.mapToJson((Map) body, true).getBytes();
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
                    byteBody = MessageUtil.mapToJson((Map) body, true).getBytes();
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
