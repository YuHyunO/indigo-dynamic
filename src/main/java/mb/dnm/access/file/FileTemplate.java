package mb.dnm.access.file;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileType;

@Setter @Getter
public class FileTemplate {
    private String templateName; // FTP관련 서비스에서 사용하는 경우에는 FTPClientTemplate의 templateName 과 동일하게 작성

    private String localSendDir;
    private String localReceiveDir;
    private String localTempDir;
    private String localSuccessDir;
    private String localErrorDir;
    private String localBackupDir;
    private String localMoveDir;
    private String localCopyDir;

    private String remoteSendDir;
    private String remoteReceiveDir;
    private String remoteTempDir;
    private String remoteSuccessDir;
    private String remoteErrorDir;
    private String remoteBackupDir;
    private String remoteMoveDir;
    private String remoteCopyDir;

    private String fileNamePattern = "*";
    private FileType type = FileType.ALL;

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
            case REMOTE_SEND: return remoteSendDir;
            case REMOTE_RECEIVE: return remoteReceiveDir;
            case REMOTE_TEMP: return remoteTempDir;
            case REMOTE_SUCCESS: return remoteSuccessDir;
            case REMOTE_ERROR: return remoteErrorDir;
            case REMOTE_BACKUP: return remoteBackupDir;
            case REMOTE_MOVE: return remoteMoveDir;
            case REMOTE_COPY: return remoteCopyDir;
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
            case REMOTE_SEND: this.remoteSendDir = dir; break;
            case REMOTE_RECEIVE: this.remoteReceiveDir = dir; break;
            case REMOTE_TEMP: this.remoteTempDir = dir; break;
            case REMOTE_SUCCESS: this.remoteSuccessDir = dir; break;
            case REMOTE_ERROR: this.remoteErrorDir = dir; break;
            case REMOTE_BACKUP: this.remoteBackupDir = dir; break;
            case REMOTE_MOVE: this.remoteMoveDir = dir; break;
            case REMOTE_COPY: this.remoteCopyDir = dir; break;
        }
    }

    public void setFileNamePattern(String fileNamePattern) {
        if (fileNamePattern == null) {
            throw new NullPointerException("fileNamePattern is null");
        }
    }
}
