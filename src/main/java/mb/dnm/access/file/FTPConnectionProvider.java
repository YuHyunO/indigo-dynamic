package mb.dnm.access.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FTPConnectionProvider {
    private static FTPConnectionProvider instance;
    private Map<String, FTPClientTemplate> templateMap;
    private boolean initilized = false;

    public FTPConnectionProvider() {
        if (instance == null) {
            instance = this;
            instance.templateMap = new HashMap<>();
        }

    }

    public static FTPConnectionProvider access() {
        if (instance == null) {
            new FTPConnectionProvider();
        }
        return instance;
    }

    public void setFtpClientTemplates(List<FTPClientTemplate> ftpClientTemplates) {
        if (!initilized) {
            for (FTPClientTemplate template : ftpClientTemplates) {
                String name = template.getTemplateName();
                if (name == null || name.isEmpty()) {
                    throw new IllegalArgumentException("FTPClientTemplate name is null or empty");
                }
                if (templateMap.containsKey(name)) {
                    throw new IllegalArgumentException("duplicate FTPClientTemplate name: " + name);
                }

                String host = template.getHost();
                if (host == null || host.isEmpty()) {
                    throw new IllegalArgumentException("FTPClientTemplate host is null or empty");
                }

                int port = template.getPort();
                if (port < 0 || port > 65535) {
                    throw new IllegalArgumentException("FTPClientTemplate port is out of range: " + port);
                }
                templateMap.put(name, template);
            }
        }
    }

    public FTPClientTemplate getFtpClientTemplate(String name) {
        return templateMap.get(name);
    }

    public Map<String, FTPClientTemplate> getFtpClientTemplateMap() {
        return templateMap;
    }
}
