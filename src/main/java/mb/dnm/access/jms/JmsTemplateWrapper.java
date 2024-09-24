package mb.dnm.access.jms;

import lombok.Getter;
import lombok.Setter;
import org.springframework.jms.core.JmsTemplate;


@Setter @Getter
public class JmsTemplateWrapper extends JmsTemplate {
    private String templateName;

}
