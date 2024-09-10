package mb.dnm.access.file;

import lombok.Getter;
import mb.dnm.code.FileType;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;

@Getter
public class FileInfo {
    private String path;
    private FileType type;
    private String extension;
    private long size;
    private long lastModified;

    public FileInfo(File file) {
        if(file == null)
            throw new NullPointerException("The File object is null");
        if (!file.exists())
            throw new IllegalStateException(file + "is not exist");

        path = file.getAbsolutePath();
        type = file.isDirectory() ? FileType.DIRECTORY : FileType.FILE;
        extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        size = file.length();
        lastModified = file.lastModified();
    }

    public FileInfo(FTPFile file) {
        path = file.getName();
    }



}
