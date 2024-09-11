package mb.dnm.access.file;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.code.FileType;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;

@Setter
@Getter
public class FileInfo {
    private String parentDir;
    private String filename;
    private FileType type;
    private String extension;
    private long size;
    private long lastModified;

    public FileInfo(File file) {
        if(file == null)
            throw new NullPointerException("The File object is null");
        if (!file.exists())
            throw new IllegalStateException(file + "is not exist");

        filename = file.getName();
        parentDir = file.getParent();
        type = file.isDirectory() ? FileType.DIRECTORY : FileType.FILE;
        extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        size = file.length();
        lastModified = file.lastModified();
    }

    public FileInfo() {
    }



}
