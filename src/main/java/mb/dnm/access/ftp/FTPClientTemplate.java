package mb.dnm.access.ftp;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.Serializable;

/**
 * FTP 서버에 접속하기 위한 정보에 대한 객체
 */
@Slf4j
@Setter @Getter
public class FTPClientTemplate implements Serializable {
    private static final long serialVersionUID = 3036621860501209905L;
    private String templateName;
    private String host;
    private int port = 21;
    private String user;
    private String password;
    private String controlEncoding = "UTF-8";
    private String serverLanguageCode;
    /**
     * UNIX<br>
     * UNIX_LTRIM<br>
     * VMS<br>
     * WINDOWS<br>
     * OS/2<br>
     * OS/400<br>
     * AS/400<br>
     * MVS<br>
     * TYPE: L8<br>
     * NETWARE<br>
     * MACOS PETER<br>
     * */
    private String serverKey;
    private int fileType = FTPClient.BINARY_FILE_TYPE;
    private boolean debugCommandAndReply = false;

    /**
     * Login ftp client.
     *
     * @return the ftp client
     * @throws IOException the io exception
     */
    public FTPClient login() throws IOException {
        FTPClient ftp = new FTPClient();
        FTPClientConfig config = null;
        if (serverKey != null) {
            config = new FTPClientConfig(serverKey.trim());
        } else {
            config = new FTPClientConfig();
        }

        if (serverLanguageCode != null) {
            config.setServerLanguageCode(serverLanguageCode);
        }


        ftp.configure(config);

        if (debugCommandAndReply) {
            ftp.addProtocolCommandListener(new ProtocolCommandListener() {
                @Override
                public void protocolCommandSent(ProtocolCommandEvent protocolCommandEvent) {
                    log.debug("[FTP Command Log]host(port): {}({}), Command sent: [{}]-{}"
                            , host, port, protocolCommandEvent.getCommand(), protocolCommandEvent.getMessage().trim());
                }

                @Override
                public void protocolReplyReceived(ProtocolCommandEvent protocolCommandEvent) {
                    log.debug("[FTP Command Log]host(port): {}({}), Reply received: {}"
                            , host, port, protocolCommandEvent.getMessage().trim());
                }
            });
        }

        try {
            ftp.setControlEncoding(controlEncoding); //This has to be set before the connection is established.
            ftp.connect(host, port);

            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new IOException("FTP server refused connection.");
            }

            if (!ftp.login(user, password)) {
                ftp.disconnect();
                throw new IOException("The username or password is incorrect.");
            }

            ftp.enterLocalPassiveMode();
            ftp.setFileType(fileType);

        } catch (Throwable t) {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
            throw t;
        }

        return ftp;
    }

    /**
     * Gets connection.
     *
     * @return the connection
     * @throws IOException the io exception
     */
    public FTPClient getConnection() throws IOException {
        return login();
    }

    /**
     * Sets file type.
     *
     * @param fileType the file type
     */
    public void setFileType(String fileType) {
        switch (fileType.toUpperCase()) {
            case "ASCII_FILE_TYPE": this.fileType = FTPClient.ASCII_FILE_TYPE; break;
            case "BINARY_FILE_TYPE": this.fileType = FTPClient.BINARY_FILE_TYPE; break;
            default: throw new IllegalArgumentException("Not supported file type: " + fileType);
        }
    }

    /**
     * template 명을 지정한다.
     *
     * @param templateName the template name
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * FTP 서버의 host를 설정한다.
     *
     * @param host the host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * FTP 서버의 port를 설정한다.
     *
     * @param port the port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * FTP 서버의 user 를 설정한다.
     *
     * @param user the user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * FTP 서버의 password를 설정한다.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * FTP 서버의 control encoding을 설정한다.
     *
     * @param controlEncoding the control encoding
     */
    public void setControlEncoding(String controlEncoding) {
        this.controlEncoding = controlEncoding;
    }

    /**
     * FTP 서버의  language code를 설정한다.<br>
     * Example: ko_KR, en_US, etc.
     *
     * @param serverLanguageCode the server language code
     */
    public void setServerLanguageCode(String serverLanguageCode) {
        this.serverLanguageCode = serverLanguageCode;
    }

    /**
     * FTP 서버의 OS 타입을 설정한다.
     * <br>
     * <br>
     *      * UNIX<br>
     *      * UNIX_LTRIM<br>
     *      * VMS<br>
     *      * WINDOWS<br>
     *      * OS/2<br>
     *      * OS/400<br>
     *      * AS/400<br>
     *      * MVS<br>
     *      * TYPE: L8<br>
     *      * NETWARE<br>
     *      * MACOS PETER<br>
     * @param serverKey the server key
     */
    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    /**
     * Sets file type.
     *
     * @param fileType the file type
     */
    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    /**
     * Sets debug command and reply.
     *
     * @param debugCommandAndReply the debug command and reply
     */
    public void setDebugCommandAndReply(boolean debugCommandAndReply) {
        this.debugCommandAndReply = debugCommandAndReply;
    }
}
