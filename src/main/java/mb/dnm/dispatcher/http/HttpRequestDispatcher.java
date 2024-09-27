package mb.dnm.dispatcher.http;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.http.HttpServletRequestEntity;
import mb.dnm.core.ServiceProcessor;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.storage.StorageManager;
import org.eclipse.jetty.util.IO;

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
 * HEADER_OUTPUT, PARAMETER_OUTPUT, BODY_OUTPUT 으로 접근가능하다.<br><br>
 *
 * @author Yuhyun O
 * @version 2024.09.27
 *
 * */
@Slf4j
public class HttpRequestDispatcher extends HttpServlet {
    /**
     * $http_headers
     * */
    private static final String HEADER_OUTPUT = "$http_headers";
    /**
     * $http_parameters
     * */
    private static final String PARAMETER_OUTPUT = "$http_parameters";
    /**
     * $http_body
     * */
    private static final String BODY_OUTPUT = "$http_body";
    /**
     * $http_servlet_response
     * */
    private static final String SERVLET_RESPONSE = "$http_servlet_response";



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

            ctx.addContextParam(HEADER_OUTPUT, requestEntity.getHeaders());
            ctx.addContextParam(PARAMETER_OUTPUT, requestEntity.getParameters());
            ctx.addContextParam(BODY_OUTPUT, requestEntity.getByteArrayBody());
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
