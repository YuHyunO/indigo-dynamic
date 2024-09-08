package mb.dnm.access.file;

public class FTPConnectionProvider {
    private static FTPConnectionProvider instance;


    public FTPConnectionProvider() {
        if (instance == null) {
            instance = this;
        }

    }

    public static FTPConnectionProvider access() {
        if (instance == null) {
            new FTPConnectionProvider();
        }
        return instance;
    }
}
