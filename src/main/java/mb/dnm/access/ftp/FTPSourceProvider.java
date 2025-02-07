package mb.dnm.access.ftp;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Ftp source provider.
 */
public class FTPSourceProvider implements Serializable {
    private static final long serialVersionUID = 6799547639065870156L;
    private static FTPSourceProvider instance;
    private Map<String, FTPClientTemplate> templateMap;
    private boolean initialized = false;

    /**
     * Instantiates a new Ftp source provider.
     */
    /*
     * Spring version 만 맞다면 private 으로 변경해도 bean으로 등록 가능함
     * */
    public FTPSourceProvider() {
        if (instance == null) {
            instance = this;
            instance.templateMap = new HashMap<>();
        }

    }

    /**
     * Access ftp source provider.
     *
     * @return the ftp source provider
     */
    public static FTPSourceProvider access() {
        if (instance == null) {
            new FTPSourceProvider();
        }
        return instance;
    }

    /**
     * Gets new session.
     *
     * @param name the name
     * @return the new session
     * @throws IOException the io exception
     */
    public FTPSession getNewSession(String name) throws IOException {
        FTPClientTemplate template = templateMap.get(name);
        if (template == null) {
            throw new NullPointerException("There is no FTPClientTemplate with name " + name);
        }
        return new FTPSession(template.login());
    }

    /**
     * Sets ftp clients.
     *
     * @param ftpClientTemplates the ftp client templates
     */
    public void setFtpClients(List<FTPClientTemplate> ftpClientTemplates) {
        if (!initialized) {
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

    /**
     * Gets ftp client template.
     *
     * @param name the name
     * @return the ftp client template
     */
    public FTPClientTemplate getFtpClientTemplate(String name) {
        return templateMap.get(name);
    }

    /**
     * Gets ftp client template map.
     *
     * @return the ftp client template map
     */
    public Map<String, FTPClientTemplate> getFtpClientTemplateMap() {
        return templateMap;
    }
}
