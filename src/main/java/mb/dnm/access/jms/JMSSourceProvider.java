package mb.dnm.access.jms;

import org.springframework.jms.core.JmsTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Jms source provider.
 */
public class JMSSourceProvider {
    private static JMSSourceProvider instance;
    private Map<String, JmsTemplateWrapper> templateMap;
    private boolean initialized = false;

    /**
     * Instantiates a new Jms source provider.
     */
    /*
     * Spring version 만 맞다면 private 생성자로 변경해도 bean으로 등록 가능함
     * */
    public JMSSourceProvider() {
        if (instance == null) {
            instance = this;
            templateMap = new HashMap<>();
        }
    }

    /**
     * Access jms source provider.
     *
     * @return the jms source provider
     */
    public static JMSSourceProvider access() {
        if (instance == null) {
            new JMSSourceProvider();
        }
        return instance;
    }

    /**
     * Gets template.
     *
     * @param templateName the template name
     * @return the template
     */
    public JmsTemplate getTemplate(String templateName) {
        return templateMap.get(templateName);
    }

    /**
     * Sets jms templates.
     *
     * @param templates the templates
     */
    public void setJmsTemplates(List<JmsTemplateWrapper> templates) {
        if (!initialized) {
            for (JmsTemplateWrapper template : templates) {
                String name = template.getTemplateName();

                if (name == null || name.isEmpty()) {
                    throw new IllegalArgumentException("The jms template name is null or empty");
                }

                if (templateMap.containsKey(name)) {
                    throw new IllegalArgumentException("duplicate jms template name: " + name);
                }

                templateMap.put(name, template);
            }

            initialized = true;
            return;
        }
        throw new IllegalStateException("DataSourceProvider is already initialized");
    }

}
