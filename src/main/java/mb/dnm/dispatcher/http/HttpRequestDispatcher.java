package mb.dnm.dispatcher.http;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.http.HttpServletRequestEntity;
import mb.dnm.core.ServiceProcessor;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;



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

    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Server", "Indigo-API");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispatch(request, response);
        response.setHeader("Server", "Indigo-API");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispatch(request, response);
        response.setHeader("Server", "Indigo-API");
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispatch(request, response);
        response.setHeader("Server", "Indigo-API");
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispatch(request, response);
        response.setHeader("Server", "Indigo-API");
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doOptions(request, response);
        response.setHeader("Server", "Indigo-API");
    }

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getRequestURI();
        String method = request.getMethod();
        InterfaceInfo info = StorageManager.access().getInterfaceInfoOfHttpRequest(url, method);

        if (info == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!info.isPermittedHttpMethod(method)) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        try {
            HttpServletRequestEntity requestEntity = new HttpServletRequestEntity(request);
            ServiceContext ctx = new ServiceContext(info);

            ctx.addContextParam(HTTP_HEADERS, requestEntity.getHeaders());
            ctx.addContextParam(HTTP_PARAMETERS, requestEntity.getParameters());
            ctx.addContextParam(HTTP_BODY, requestEntity.getByteArrayBody());
            ctx.addContextParam(SERVLET_RESPONSE, response);

            String txId = ctx.getTxId();
            log.info("[{}]A new interface transaction was created", txId);
            ServiceProcessor.unfoldServices(ctx);
            log.info("[{}]The interface transaction was ended", txId);

        } catch (IOException ie) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("HttpRequestHandling failed", ie);
        }


    }

}
