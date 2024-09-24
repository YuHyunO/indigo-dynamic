package com.mb.jms;

import com.indigo.indigomq.IndigoMQConnectionFactory;
import com.indigo.indigomq.pool.PooledConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.jms.IndigoJmsTemplateWrapper;
import org.junit.Test;

@Slf4j
public class IndigoJmsTemplateMapperTest {


    @Test
    public void setConnectionFactory_test() {
        String url = "failover:(tcp://127.0.0.1:24211)?initialReconnectDelay=10&maxReconnectDelay=30000&maxReconnectAttempts=0&randomize=true";

        IndigoJmsTemplateWrapper wrapper = new IndigoJmsTemplateWrapper();
        String renewUrl = wrapper.getTimeoutParamAddedUrl(url, "timeout", 100);
        log.info("renewUrl: {}", renewUrl);

        IndigoMQConnectionFactory mqcf = new IndigoMQConnectionFactory();
        mqcf.setBrokerURL(url);
        PooledConnectionFactory pcp = new PooledConnectionFactory(mqcf);

        wrapper.setConnectionFactory(pcp);
        wrapper.setConnectTimeoutMillis(5000);

    }
}
