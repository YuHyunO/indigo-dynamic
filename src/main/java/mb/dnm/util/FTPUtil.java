package mb.dnm.util;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;

public class FTPUtil {
    private FTPUtil() {}

    public static boolean isDirectoryExists(FTPClient ftp, String dirPath) throws IOException {
        String pwd = ftp.printWorkingDirectory();
        ftp.changeWorkingDirectory(dirPath);
        int returnCode = ftp.getReplyCode();
        if (returnCode == 550) {
            return false;
        }
        ftp.changeWorkingDirectory(pwd);
        return true;
    }

    public static boolean isFileExists(FTPClient ftp, String filePath) throws IOException {
        InputStream inputStream = ftp.retrieveFileStream(filePath);
        int returnCode = ftp.getReplyCode();
        if (inputStream == null || returnCode == 550) {
            return false;
        }
        return true;
    }

}
