package mb.dnm.access.file;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

@Slf4j
@Setter @Getter
public class FTPClientTemplate {
    private String templateName;
    private String host;
    private int port = 21;
    private String user;
    private String password;
    private String controlEncoding = "UTF-8";
    private FTPClient ftp;

    public FTPClient login() throws IOException {
        if (ftp == null || !ftp.isConnected()) {
            ftp = new FTPClient();
            ftp.addProtocolCommandListener(new ProtocolCommandListener() {
                @Override
                public void protocolCommandSent(ProtocolCommandEvent protocolCommandEvent) {
                    log.debug("[FTP Login]host(port): {}({}), Command sent: [{}]-{}"
                            , host, port, protocolCommandEvent.getCommand(), protocolCommandEvent.getMessage());
                }

                @Override
                public void protocolReplyReceived(ProtocolCommandEvent protocolCommandEvent) {
                    log.debug("[FTP Login]host(port): {}({}), Reply received: {}"
                            , host, port, protocolCommandEvent.getMessage());
                }
            });

            try {
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

                ftp.setControlEncoding(controlEncoding);
                ftp.enterLocalPassiveMode();
            } catch (Throwable t) {
                if (ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
        return ftp;
    }

    public FTPClient getConnection() throws IOException {
        if (ftp == null || !ftp.isConnected()) {
            return login();
        }
        return ftp;
    }

    public boolean logout() {
        if (ftp == null || !ftp.isConnected()) {
            return true;
        }

        boolean loggedOut = true;
        try {
            loggedOut = ftp.logout();
        } catch (Throwable t) {
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return loggedOut;
    }

    public boolean disconnect() {
        return logout();
    }



}
