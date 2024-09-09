package mb.dnm.access.ftp;

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

    public FTPClient login() throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.addProtocolCommandListener(new ProtocolCommandListener() {
            @Override
            public void protocolCommandSent(ProtocolCommandEvent protocolCommandEvent) {
                log.debug("[FTP Command Log]host(port): {}({}), Command sent: [{}]-{}"
                        , host, port, protocolCommandEvent.getCommand(), protocolCommandEvent.getMessage().replace("\n", ""));
            }

            @Override
            public void protocolReplyReceived(ProtocolCommandEvent protocolCommandEvent) {
                log.debug("[FTP Command Log]host(port): {}({}), Reply received: {}"
                        , host, port, protocolCommandEvent.getMessage().replace("\n", ""));
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

        return ftp;
    }

    public FTPClient getConnection() throws IOException {
        return login();
    }

}
