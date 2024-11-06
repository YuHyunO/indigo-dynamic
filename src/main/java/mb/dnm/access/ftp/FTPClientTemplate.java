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
     *
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

    public FTPClient getConnection() throws IOException {
        return login();
    }

    public void setFileType(String fileType) {
        switch (fileType.toUpperCase()) {
            case "ASCII_FILE_TYPE": this.fileType = FTPClient.ASCII_FILE_TYPE; break;
            case "BINARY_FILE_TYPE": this.fileType = FTPClient.BINARY_FILE_TYPE; break;
            default: throw new IllegalArgumentException("Not supported file type: " + fileType);
        }
    }

}
