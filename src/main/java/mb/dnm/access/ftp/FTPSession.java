package mb.dnm.access.ftp;

import mb.dnm.access.ClosableStreamWrapper;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.Serializable;

public class FTPSession implements ClosableStreamWrapper, Serializable {
    private static final long serialVersionUID = -6338893611571699317L;
    private FTPClient ftp;

    public FTPSession(FTPClient ftp) {
        this.ftp = ftp;
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

    @Override
    public boolean close() {
        return logout();
    }

    @Override
    public boolean isConnected() {
        if (ftp == null || !ftp.isConnected()) {
            return false;
        }
        return true;
    }

    public FTPClient getFTPClient() {
        return ftp;
    }

}
