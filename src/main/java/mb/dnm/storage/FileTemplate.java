package mb.dnm.storage;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.code.FileType;

@Setter @Getter
public class FileTemplate {
    private String templateName;

    private String localSendDir;
    private String localReceiveDir;
    private String localTempDir;
    private String localErrorDir;

    private String remoteSendDir;
    private String remoteReceiveDir;
    private String remoteTempDir;
    private String remoteErrorDir;

    private String fileNamePattern;
    private FileType type = FileType.ALL;



}
