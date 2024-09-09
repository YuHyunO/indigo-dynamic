package mb.dnm.access;

/**
 * A ClosableSession is a IO Stream that can be closed. The close method is invoked to close all streams underlying a ClosableSession.
 * */
public interface ClosableSession {
    /**
     * Close all streams of a ClosableSession
     * */
    public boolean close();

    public boolean isConnected();
}
