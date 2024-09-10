package mb.dnm.storage;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.code.FileType;

@Setter @Getter
public class FileTemplate {
    private String templateName;
    private String localPath;
    private String remotePath;
    private String fileNamePattern;
    private FileType type = FileType.ALL;



}
