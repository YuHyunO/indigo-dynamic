package mb.dnm.service.jms;

import mb.dnm.access.jms.JMSSourceProvider;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import org.springframework.jms.core.JmsTemplate;

public abstract class AbstractJMSService extends SourceAccessService {

    protected JmsTemplate getJmsTemplate(String srcName) throws Throwable {
        JmsTemplate jmsTemplate = JMSSourceProvider.access().getTemplate(srcName);
        return jmsTemplate;
    }

    protected String getJmsTemplateName(InterfaceInfo info) {
        return super.getSourceName(info);
    }

}
