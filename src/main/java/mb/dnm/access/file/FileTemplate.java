package mb.dnm.access.file;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.code.DataType;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileContentType;
import mb.dnm.code.FileType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Setter @Getter
public class FileTemplate implements Serializable {
    private static final long serialVersionUID = -5683892050317874131L;
    /**
     * <code>FileTemplate</code> 의 이름이다.<br>
     * <code>FTPSourceProvider</code> 나 <code>InterfaceInfo</code> 에 등록되어 사용될 때는 이름이 고유한 값으로 지정되어야 한다.
     * FTP 관련 서비스에서 참조하는 객체로 사용될 때는 <code>FTPClientTemplate</code>의 templateName 과 동일해야한다.
     * @see InterfaceInfo
     * @see mb.dnm.access.ftp.FTPSourceProvider
     * @see mb.dnm.access.ftp.FTPClientTemplate
     * */
    private String templateName; // FTP관련 서비스에서 사용하는 경우에는 FTPClientTemplate의 templateName 과 동일하게 작성

    private String localSendDir;
    private String localReceiveDir;
    private String localTempDir;
    private String localSuccessDir;
    private String localErrorDir;
    private String localBackupDir;
    private String localMoveDir;
    private String localCopyDir;
    private String localWriteDir;

    private String remoteSendDir;
    private String remoteReceiveDir;
    private String remoteTempDir;
    private String remoteSuccessDir;
    private String remoteErrorDir;
    private String remoteBackupDir;
    private String remoteMoveDir;
    private String remoteCopyDir;
    private String remoteWriteDir;

    private String fileNamePattern = "*";
    private FileType type = FileType.ALL;

    /**
     * 파일을 생성할 때 파일명으로 사용되는 속성이다
     * @see mb.dnm.service.file.WriteFile
     * */
    private String fileName;
    /**
     * 파일을 생성할 때의 인코딩으로 사용되는 속성이다.
     * 파일의 charset 설정은 FileContentType 이 TEXT인 경우에만 적용된다.
     * @see mb.dnm.service.file.WriteFile
     * */
    private Charset charset = StandardCharsets.UTF_8;

    private DataType dataType = DataType.BYTE_ARRAY;

    public String getFileName(ServiceContext ctx) {
        if (fileName == null)
            return null;
        String tmpFileName = fileName.replace("@{if_id}", ctx.getInterfaceId());
        //PlaceHolderMapper를 적용

        return fileName;
    }

    public void setCharset(String charset) {
        this.charset = Charset.forName(charset);
    }

    public String getFilePath(DirectoryType dirType) {
        switch (dirType) {
            case LOCAL_SEND: return localSendDir;
            case LOCAL_RECEIVE: return localReceiveDir;
            case LOCAL_TEMP: return localTempDir;
            case LOCAL_SUCCESS: return localSuccessDir;
            case LOCAL_ERROR: return localErrorDir;
            case LOCAL_BACKUP: return localBackupDir;
            case LOCAL_MOVE: return localMoveDir;
            case LOCAL_COPY: return localCopyDir;
            case LOCAL_WRITE: return localWriteDir;
            case REMOTE_SEND: return remoteSendDir;
            case REMOTE_RECEIVE: return remoteReceiveDir;
            case REMOTE_TEMP: return remoteTempDir;
            case REMOTE_SUCCESS: return remoteSuccessDir;
            case REMOTE_ERROR: return remoteErrorDir;
            case REMOTE_BACKUP: return remoteBackupDir;
            case REMOTE_MOVE: return remoteMoveDir;
            case REMOTE_COPY: return remoteCopyDir;
            case REMOTE_WRITE: return remoteWriteDir;
            default: return null;
        }
    }

    public void setFilePath(DirectoryType dirType, String dir) {
        switch (dirType) {
            case LOCAL_SEND: this.localSendDir = dir; break;
            case LOCAL_RECEIVE: this.localReceiveDir = dir; break;
            case LOCAL_TEMP: this.localTempDir = dir; break;
            case LOCAL_SUCCESS:this.localSuccessDir = dir; break;
            case LOCAL_ERROR: this.localErrorDir = dir; break;
            case LOCAL_BACKUP: this.localBackupDir = dir; break;
            case LOCAL_MOVE: this.localMoveDir = dir; break;
            case LOCAL_COPY: this.localCopyDir = dir; break;
            case LOCAL_WRITE: this.localWriteDir = dir; break;
            case REMOTE_SEND: this.remoteSendDir = dir; break;
            case REMOTE_RECEIVE: this.remoteReceiveDir = dir; break;
            case REMOTE_TEMP: this.remoteTempDir = dir; break;
            case REMOTE_SUCCESS: this.remoteSuccessDir = dir; break;
            case REMOTE_ERROR: this.remoteErrorDir = dir; break;
            case REMOTE_BACKUP: this.remoteBackupDir = dir; break;
            case REMOTE_MOVE: this.remoteMoveDir = dir; break;
            case REMOTE_COPY: this.remoteCopyDir = dir; break;
            case REMOTE_WRITE: this.remoteWriteDir = dir; break;
        }
    }

    public void setFileNamePattern(String fileNamePattern) {
        if (fileNamePattern == null) {
            throw new NullPointerException("fileNamePattern is null");
        }
        this.fileNamePattern = fileNamePattern;
    }
}
