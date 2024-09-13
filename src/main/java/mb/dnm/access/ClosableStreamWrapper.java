package mb.dnm.access;


public interface ClosableStreamWrapper {

    public boolean close();

    public boolean isConnected();
}
