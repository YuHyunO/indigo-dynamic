package mb.dnm.access.file;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.code.DataType;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.storage.InterfaceInfo;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * File 인터페이스 관련 설정 정보(파일 경로/파일명 등)를 저장하는 객체이다.<br><br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 *&lt;bean class="mb.dnm.access.file.FileTemplate"&gt;
 *	&lt;property name="templateName"        value="<span style="color: black; background-color: #FAF3D4;">Template 명</span>"/&gt;
 *	&lt;property name="localSendDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- LOCAL_SEND --&gt;
 *	&lt;property name="localReceiveDir"     value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- LOCAL_RECEIVE --&gt;
 *	&lt;property name="localTempDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- LOCAL_TEMP --&gt;
 *	&lt;property name="localSuccessDir"     value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- LOCAL_SUCCESS --&gt;
 *	&lt;property name="localErrorDir"       value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- LOCAL_ERROR --&gt;
 *	&lt;property name="localBackupDir"      value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- LOCAL_BACKUP --&gt;
 *	&lt;property name="localMoveDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- LOCAL_MOVE --&gt;
 *	&lt;property name="localCopyDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- LOCAL_COPY --&gt;
 *	&lt;property name="localWriteDir"       value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- LOCAL_WRITE --&gt;
 *	&lt;property name="remoteSendDir"       value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- REMOTE_SEND --&gt;
 *	&lt;property name="remoteReceiveDir"    value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- REMOTE_RECEIVE --&gt;
 *	&lt;property name="remoteTempDir"       value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- REMOTE_TEMP --&gt;
 *	&lt;property name="remoteSuccessDir"    value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- REMOTE_SUCCESS --&gt;
 *	&lt;property name="remoteErrorDir"      value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- REMOTE_ERROR --&gt;
 *	&lt;property name="remoteBackupDir"     value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- REMOTE_BACKUP --&gt;
 *	&lt;property name="remoteMoveDir"       value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- REMOTE_MOVE --&gt;
 *	&lt;property name="remoteCopyDir"       value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- REMOTE_COPY --&gt;
 *	&lt;property name="remoteWriteDir"      value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt; &lt;!-- REMOTE_WRITE --&gt;
 *	&lt;property name="fileNamePattern"     value="<span style="color: black; background-color: #FAF3D4;">파일명 패턴</span>"/&gt;
 *	&lt;property name="type"                value="<span style="color: black; background-color: #FAF3D4;">파일 타입</span>"/&gt;
 *	&lt;property name="dataType"            value="<span style="color: black; background-color: #FAF3D4;">파일 데이터 타입</span>"/&gt;
 *	&lt;property name="charset"             value="<span style="color: black; background-color: #FAF3D4;">파일 인코딩</span>"/&gt;
 *&lt;/bean&gt;
 *</pre>
 * <br><br>
 * <i>Example</i>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 *&lt;bean class="mb.dnm.access.file.FileTemplate"&gt;
 * 	&lt;property name="templateName"        value="LTR_FTP"/&gt;
 * 	&lt;property name="remoteSendDir"       value="/rec/cur"/&gt;
 * 	&lt;property name="remoteSuccessDir"    value="/rec/cur/succ/@{YYYYMMDD}"/&gt;
 * 	&lt;property name="remoteErrorDir"      value="/rec/cur/err/@{YYYYMMDD}"/&gt;
 * 	&lt;property name="localTempDir"        value="/app/indigo/FILE_WORK/LTR/@{if_id}"/&gt;
 * 	&lt;property name="fileNamePattern"     value="RA0*"/&gt;
 * 	&lt;property name="type"                value="FILE"/&gt;
 * 	&lt;property name="dataType"            value="STRING"/&gt;
 * 	&lt;property name="charset"             value="MS949"/&gt;
 *&lt;/bean&gt;
 *</pre>
 */
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
     * @see DirectoryType
     * */
    private String templateName; // FTP관련 서비스에서 사용하는 경우에는 FTPClientTemplate의 templateName 과 동일하게 작성
    /**
     * {@code DirectoryType.LOCAL_SEND}
     * */
    private String localSendDir;
    /**
     {@link DirectoryType#LOCAL_RECEIVE}
     * */
    private String localReceiveDir;
    /**
     {@link DirectoryType#LOCAL_TEMP}
     * */
    private String localTempDir;
    /**
     {@link DirectoryType#LOCAL_SUCCESS}
     * */
    private String localSuccessDir;
    /**
     {@link DirectoryType#LOCAL_ERROR}
     * */
    private String localErrorDir;
    /**
     {@link DirectoryType#LOCAL_BACKUP}
     * */
    private String localBackupDir;
    /**
     {@link DirectoryType#LOCAL_MOVE}
     * */
    private String localMoveDir;
    /**
     {@link DirectoryType#LOCAL_COPY}
     * */
    private String localCopyDir;
    /**
     {@link DirectoryType#LOCAL_WRITE}
     * */
    private String localWriteDir;
    /**
     {@link DirectoryType#REMOTE_SEND}
     * */
    private String remoteSendDir;
    /**
     {@link DirectoryType#REMOTE_RECEIVE}
     * */
    private String remoteReceiveDir;
    /**
     {@link DirectoryType#REMOTE_TEMP}
     * */
    private String remoteTempDir;
    /**
     {@link DirectoryType#REMOTE_SUCCESS}
     * */
    private String remoteSuccessDir;
    /**
     {@link DirectoryType#REMOTE_ERROR}
     * */
    private String remoteErrorDir;
    /**
     {@link DirectoryType#REMOTE_BACKUP}
     * */
    private String remoteBackupDir;
    /**
     {@link DirectoryType#REMOTE_MOVE}
     * */
    private String remoteMoveDir;
    /**
     {@link DirectoryType#REMOTE_COPY}
     * */
    private String remoteCopyDir;
    /**
     {@link DirectoryType#REMOTE_WRITE}
     * */
    private String remoteWriteDir;

    private String fileNamePattern = "*";
    /**
     {@link FileType#DIRECTORY}
     {@link FileType#FILE}
     {@link FileType#ALL}
     * */
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

    /**
     * {@code FileTemplate}에 설정된 파일명을 가져온다.<br><br>
     *<pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *public String getFileName(ServiceContext ctx) {
     *    if (fileName == null)
     *        return null;
     *    return fileName.replace("@{if_id}", ctx.getInterfaceId());
     *}</pre>
     * @param ctx the ctx({@link ServiceContext})
     * @return the file name
     */
    public String getFileName(ServiceContext ctx) {
        if (fileName == null)
            return null;
        return fileName.replace("@{if_id}", ctx.getInterfaceId());
    }

    /**
     * {@code DirectoryType.LOCAL_SEND} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="localSendDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param localSendDir the local send dir
     * @see DirectoryType#LOCAL_SEND
     */
    public void setLocalSendDir(String localSendDir) {
        this.localSendDir = localSendDir;
    }

    /**
     * {@code DirectoryType.LOCAL_RECEIVE} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="localReceiveDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param localReceiveDir the local receive dir
     * @see DirectoryType#LOCAL_RECEIVE
     */
    public void setLocalReceiveDir(String localReceiveDir) {
        this.localReceiveDir = localReceiveDir;
    }

    /**
     * {@code DirectoryType.LOCAL_TEMP} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="localTempDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param localTempDir the local temp dir
     * @see DirectoryType#LOCAL_TEMP
     */
    public void setLocalTempDir(String localTempDir) {
        this.localTempDir = localTempDir;
    }

    /**
     * {@code DirectoryType.LOCAL_SUCCESS} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="localSuccessDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param localSuccessDir the local success dir
     * @see DirectoryType#LOCAL_SUCCESS
     */
    public void setLocalSuccessDir(String localSuccessDir) {
        this.localSuccessDir = localSuccessDir;
    }

    /**
     * {@code DirectoryType.LOCAL_ERROR} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="localErrorDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param localErrorDir the local error dir
     * @see DirectoryType#LOCAL_ERROR
     */
    public void setLocalErrorDir(String localErrorDir) {
        this.localErrorDir = localErrorDir;
    }

    /**
     * {@code DirectoryType.LOCAL_BACKUP} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="localBackupDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param localBackupDir the local backup dir
     * @see DirectoryType#LOCAL_BACKUP
     */
    public void setLocalBackupDir(String localBackupDir) {
        this.localBackupDir = localBackupDir;
    }

    /**
     * {@code DirectoryType.LOCAL_MOVE} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="localMoveDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param localMoveDir the local move dir
     * @see DirectoryType#LOCAL_MOVE
     */
    public void setLocalMoveDir(String localMoveDir) {
        this.localMoveDir = localMoveDir;
    }

    /**
     * {@code DirectoryType.LOCAL_COPY} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="localCopyDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param localCopyDir the local copy dir
     * @see DirectoryType#LOCAL_COPY
     */
    public void setLocalCopyDir(String localCopyDir) {
        this.localCopyDir = localCopyDir;
    }

    /**
     * {@code DirectoryType.LOCAL_WRITE} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="localWriteDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param localWriteDir the local write dir
     * @see DirectoryType#LOCAL_WRITE
     */
    public void setLocalWriteDir(String localWriteDir) {
        this.localWriteDir = localWriteDir;
    }

    /**
     * {@code DirectoryType.REMOTE_SEND} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="remoteSendDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param remoteSendDir the remote send dir
     * @see DirectoryType#REMOTE_SEND
     */
    public void setRemoteSendDir(String remoteSendDir) {
        this.remoteSendDir = remoteSendDir;
    }

    /**
     * {@code DirectoryType.REMOTE_RECEIVE} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="remoteReceiveDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param remoteReceiveDir the remote receive dir
     * @see DirectoryType#REMOTE_RECEIVE
     */
    public void setRemoteReceiveDir(String remoteReceiveDir) {
        this.remoteReceiveDir = remoteReceiveDir;
    }

    /**
     * {@code DirectoryType.REMOTE_TEMP} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="remoteTempDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param remoteTempDir the remote temp dir
     * @see DirectoryType#REMOTE_TEMP
     */
    public void setRemoteTempDir(String remoteTempDir) {
        this.remoteTempDir = remoteTempDir;
    }

    /**
     * {@code DirectoryType.REMOTE_SUCCESS} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="remoteSuccessDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param remoteSuccessDir the remote success dir
     * @see DirectoryType#REMOTE_SUCCESS
     */
    public void setRemoteSuccessDir(String remoteSuccessDir) {
        this.remoteSuccessDir = remoteSuccessDir;
    }

    /**
     * {@code DirectoryType.REMOTE_ERROR} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="remoteErrorDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param remoteErrorDir the remote error dir
     * @see DirectoryType#REMOTE_ERROR
     */
    public void setRemoteErrorDir(String remoteErrorDir) {
        this.remoteErrorDir = remoteErrorDir;
    }

    /**
     * {@code DirectoryType.REMOTE_BACKUP} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="remoteBackupDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param remoteBackupDir the remote backup dir
     * @see DirectoryType#REMOTE_BACKUP
     */
    public void setRemoteBackupDir(String remoteBackupDir) {
        this.remoteBackupDir = remoteBackupDir;
    }

    /**
     * {@code DirectoryType.REMOTE_MOVE} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="remoteMoveDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param remoteMoveDir the remote move dir
     * @see DirectoryType#REMOTE_MOVE
     */
    public void setRemoteMoveDir(String remoteMoveDir) {
        this.remoteMoveDir = remoteMoveDir;
    }

    /**
     * {@code DirectoryType.REMOTE_COPY} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="remoteCopyDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param remoteCopyDir the remote copy dir
     * @see DirectoryType#REMOTE_COPY
     */
    public void setRemoteCopyDir(String remoteCopyDir) {
        this.remoteCopyDir = remoteCopyDir;
    }

    /**
     * {@code DirectoryType.REMOTE_WRITE} 에 매핑되는 디렉터리 경로를 지정한다.
     * <br><br><pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;property name="remoteWriteDir"        value="<span style="color: black; background-color: #FAF3D4;">파일 경로</span>"/&gt;</pre>
     * @param remoteWriteDir the remote write dir
     * @see DirectoryType#REMOTE_WRITE
     */
    public void setRemoteWriteDir(String remoteWriteDir) {
        this.remoteWriteDir = remoteWriteDir;
    }

    /**
     * 파일의 인코딩을 지정한다.<br><br>
     *<pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *public void setCharset(String charset) {
     *    this.charset = Charset.forName(charset);
     *}</pre>
     * @param charset the charset
     */
    public void setCharset(String charset) {
        this.charset = Charset.forName(charset);
    }

    /**
     * {@code DirectoryType}을 사용하여 매핑되는 디렉터리 경로를 가져온다.<br><br>
     *<pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *public String getFilePath(DirectoryType dirType) {
     *    switch (dirType) {
     *        case LOCAL_SEND: return getLocalSendDir();
     *        case LOCAL_RECEIVE: return getLocalReceiveDir();
     *        case LOCAL_TEMP: return getLocalTempDir();
     *        case LOCAL_SUCCESS: return getLocalSuccessDir();
     *        case LOCAL_ERROR: return getLocalErrorDir();
     *        case LOCAL_BACKUP: return getLocalBackupDir();
     *        case LOCAL_MOVE: return getLocalMoveDir();
     *        case LOCAL_COPY: return getLocalCopyDir();
     *        case LOCAL_WRITE: return getLocalWriteDir();
     *        case REMOTE_SEND: return getRemoteSendDir();
     *        case REMOTE_RECEIVE: return getRemoteReceiveDir();
     *        case REMOTE_TEMP: return getRemoteTempDir();
     *        case REMOTE_SUCCESS: return getRemoteSuccessDir();
     *        case REMOTE_ERROR: return getRemoteErrorDir();
     *        case REMOTE_BACKUP: return getRemoteBackupDir();
     *        case REMOTE_MOVE: return getRemoteMoveDir();
     *        case REMOTE_COPY: return getRemoteCopyDir();
     *        case REMOTE_WRITE: return getRemoteWriteDir();
     *        default: return null;
     *    }
     *}</pre>
     * @param dirType the dir type
     * @return the file path
     */
    public String getFilePath(DirectoryType dirType) {
        switch (dirType) {
            case LOCAL_SEND: return getLocalSendDir();
            case LOCAL_RECEIVE: return getLocalReceiveDir();
            case LOCAL_TEMP: return getLocalTempDir();
            case LOCAL_SUCCESS: return getLocalSuccessDir();
            case LOCAL_ERROR: return getLocalErrorDir();
            case LOCAL_BACKUP: return getLocalBackupDir();
            case LOCAL_MOVE: return getLocalMoveDir();
            case LOCAL_COPY: return getLocalCopyDir();
            case LOCAL_WRITE: return getLocalWriteDir();
            case REMOTE_SEND: return getRemoteSendDir();
            case REMOTE_RECEIVE: return getRemoteReceiveDir();
            case REMOTE_TEMP: return getRemoteTempDir();
            case REMOTE_SUCCESS: return getRemoteSuccessDir();
            case REMOTE_ERROR: return getRemoteErrorDir();
            case REMOTE_BACKUP: return getRemoteBackupDir();
            case REMOTE_MOVE: return getRemoteMoveDir();
            case REMOTE_COPY: return getRemoteCopyDir();
            case REMOTE_WRITE: return getRemoteWriteDir();
            default: return null;
        }
    }

    /**
     * Sets file path.
     * @param dirType the dir type
     * @param dir     the dir
     */
    @Deprecated
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

    /**
     * 파일명 패턴을 지정한다.
     *
     * @param fileNamePattern the file name pattern
     */
    public void setFileNamePattern(String fileNamePattern) {
        if (fileNamePattern == null) {
            throw new NullPointerException("fileNamePattern is null");
        }
        this.fileNamePattern = fileNamePattern;
    }


}
