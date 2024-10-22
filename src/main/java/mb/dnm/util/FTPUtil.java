package mb.dnm.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Slf4j
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
        if (isDirectoryExists(ftp, filePath)) {
            return true;
        }
        File file = new File(filePath);
        String dirPath = file.getParent();
        if (!isDirectoryExists(ftp, dirPath)) {
            return false;
        }

        String[] remoteNames = ftp.listNames(dirPath);
        if (remoteNames.length == 0) {
            return false;
        }

        String filaName = file.getName();
        for (String remoteName : remoteNames) {
            int idx = remoteName.lastIndexOf("/");
            if (idx == -1) {
                if (filaName.equals(remoteName)) {
                    return true;
                }
            } else {
                if (filaName.equals(remoteName.substring(idx + 1))) {
                    return true;
                }
            }
        }
        return false;

    }

}
