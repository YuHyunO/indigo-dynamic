package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileType;
import mb.dnm.service.ftp.FTPLogin;

/**
 * 접근 가능한 디스크의 파일 또는 디렉터리 목록을 가져온다.
 * 어느 경로의 어떤 파일 목록을 가져올 지에 대한 정보는 <code>InterfaceInfo</code> 에 저장된 <code>FileTemplate</code> 의 속성들로부터 가져온다.
 *
 * @see FTPLogin
 * @see mb.dnm.access.file.FileList
 *
 * @author Yuhyun O
 * @version 2024.09.10
 *
 * @Input List를 가져올 Directory의 경로
 * @InputType <code>String</code>
 * @Output File 또는 Directory 경로
 * @OutputType <code>FileList</code>
 * */

@Slf4j
@Setter
public class ListFiles {
    /**
     * directoryType 속성에 따라 <code>FileTemplate</code>에서 어떤 속성의 값을 목록을 가져올 경로로써 사용할 지 결정된다.<br><br>
     * -기본값: <code>REMOTE_SEND</code><br>
     * -REMOTE_SEND → <code>FileTemplate#remoteSendDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -REMOTE_RECEIVE → <code>FileTemplate#remoteReceiveDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -REMOTE_TEMP → <code>FileTemplate#remoteTempDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -REMOTE_SUCCESS → <code>FileTemplate#remoteSuccessDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -REMOTE_ERROR → <code>FileTemplate#remoteErrorDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -REMOTE_BACKUP → <code>FileTemplate#remoteBackupDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -LOCAL_SEND → <code>FileTemplate#localSendDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -LOCAL_RECEIVE → <code>FileTemplate#localReceiveDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -LOCAL_TEMP → <code>FileTemplate#localTempDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -LOCAL_SUCCESS → <code>FileTemplate#localSuccessDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -LOCAL_ERROR → <code>FileTemplate#localErrorDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * -LOCAL_BACKUP → <code>FileTemplate#localBackupDir</code> 을 파일목록을 가져올 경로로 사용함<br>
     * */
    private DirectoryType directoryType = DirectoryType.REMOTE_SEND;
    private String listDirectory;
    private String fileNamePattern;
    private FileType type = FileType.ALL;
    private String pathSeparator;
    /**
     * 기본값: false<br>
     * 파일 목록을 탐색할 경로가 디렉터리인 경우 그 디렉터리의 하부 파일들을 재귀적으로 탐색할 지에 대한 여부를 결정한다.
     * <code>type</code> 속성이 ALL 또는 DIRECTORY 인 경우에만 유효하다.
     * <code>type</code> 속성이 DIRECTORY 인 경우에 이 속성을 사용하면 파일목록을 가져올 경로로 지정한 최상위 경로에서는 디렉터리만 탐색을 하고,
     * 다시 그 디렉터리의 하위를 탐색할 때는 파일과 디렉터리 구분에 대한 필터링이 적용되지 않는다.
     * */
    private boolean searchRecursively = false;
}
