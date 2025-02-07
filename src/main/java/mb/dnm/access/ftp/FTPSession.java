package mb.dnm.access.ftp;

import mb.dnm.access.ClosableStreamWrapper;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.Serializable;

/**
 * The type Ftp session.
 */
public class FTPSession implements ClosableStreamWrapper, Serializable {
    private static final long serialVersionUID = -6338893611571699317L;
    private FTPClient ftp;

    /**
     * Instantiates a new Ftp session.
     *
     * @param ftp the ftp
     */
    public FTPSession(FTPClient ftp) {
        this.ftp = ftp;
    }

    /**
     * Logout boolean.
     *
     * @return the boolean
     */
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

    /**
     * Gets ftp client.
     *
     * @return the ftp client
     */
    public FTPClient getFTPClient() {
        return ftp;
    }

}
