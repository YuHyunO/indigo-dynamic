package mb.dnm.access.jms;

import com.indigo.indigomq.IndigoMQConnectionFactory;
import com.indigo.indigomq.pool.PooledConnectionFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.jms.ConnectionFactory;
import java.net.URI;


/**
 * IMC의 JMS 공통 리소스를 사용하는 경우 BrokerUrl 에 연결 타임아웃을 설정하는 부분이 없다.
 * 화면 수정없이 jms connect timeout 설정을 할 수 있도록 하기위한 Wrapper 클래스이다.
 * 
 * @author Yuhyun O
 * @version 2024.09.24
 * */
@Slf4j
@Setter
public class IndigoJmsTemplateWrapper extends JmsTemplateWrapper {
    int connectTimeoutMillis = -1;

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        //setConnectionFactory() 메소드와의 호출 순서 상관없이 IndigoMQConnectionFactory의 Broker URL에 timeout 파라미터를 추가하기 위한 로직
        ConnectionFactory connectionFactory = getConnectionFactory();
        if (connectionFactory != null) {
            this.setConnectionFactory(connectionFactory);
        }
    }

    @Override
    public void setConnectionFactory(ConnectionFactory connectionFactory) {

        IndigoMQConnectionFactory cp = null;
        if (connectTimeoutMillis != -1 && connectionFactory instanceof PooledConnectionFactory) {
            cp = (IndigoMQConnectionFactory) ((PooledConnectionFactory) connectionFactory).getConnectionFactory();

        } else if (connectTimeoutMillis != -1 && connectionFactory instanceof IndigoMQConnectionFactory) {
            cp = (IndigoMQConnectionFactory) connectionFactory;
        }

        if (cp != null) {
            String brokerUrl = cp.getBrokerURL();
            URI uri = URI.create(brokerUrl);
            String scheme = uri.getScheme();

            switch (scheme) {
                case "failover": cp.setBrokerURL(getTimeoutParamAddedUrl(brokerUrl,"timeout", connectTimeoutMillis)); break;
                case "nio": case "ssl" : case "stomp+nio": case "tcp": cp.setBrokerURL(getTimeoutParamAddedUrl(brokerUrl,"soTimeout", connectTimeoutMillis)); break;
                default: break;
            }

            log.info("Broker URL: [{}]", cp.getBrokerURL());
        }

        super.setConnectionFactory(connectionFactory);
    }

    public String getTimeoutParamAddedUrl(String url, String paramName, int timeout) {
        if (url.contains(paramName)) {
            return url;
        }
        StringBuffer urlBuffer = new StringBuffer(url);
        String paramForm = paramName + "=" + timeout;
        int questionMarkIdx = url.indexOf('?');
        if (questionMarkIdx != -1) {
            urlBuffer.insert(questionMarkIdx + 1, paramForm + "&");
        } else {
            urlBuffer.append('?').append(paramForm);
        }

        return urlBuffer.toString();
    }
}
