package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.code.DirectoryType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.service.SourceAccessService;

/**
 * 지정된 경로에 파일을 생성한다.
 * <br>
 * 생성할
 * <br>
 * 어느 경로에 생성한 파일을 저장할 지에 대한 정보는 <code>InterfaceInfo</code> 에 저장된 <code>FileTemplate</code> 의 속성들로부터 가져온다.
 * 파일내용을 텍스트로 쓸 것인지 바이트배열로 쓸 것인지, 텍스트로 쓴다면 어떤 인코딩으로 생성할지에 대한 정보또한 <code>FileTemplate</code> 의 속성들로부터 가져온다.
 *
 * @see mb.dnm.access.file.FileTemplate
 *
 * @author Yuhyun O
 * @version 2024.09.18
 *
 * @Input 이동할 파일의 경로
 * @InputType <code>String</code>(1건) 또는 <code>List&lt;String&gt;</code> 또는 <code>Set&lt;String&gt;</code> 또는 <code>FileList</code><br>
 * input이 List로 전달되는 경우 중복된 경로가 존재하더라도 내부적으로 Set 객체에 다시 담기게 되므로 중복값이 제거된다.
 * @Output 생성한 파일의 저장 경로
 * @OutputType <code>List&lt;String&gt;</code>
 * @ErrorOutput 파일을 이동하는 중 에러가 발생하여 이동에 실패하는 경우 에러가 발생한 파일의 경로
 * @ErrorOutputType <code>List&lt;String&gt;</code>
 * */
@Slf4j
@Setter
public class WriteFiles extends SourceAccessService {
    /**
     * input으로 전달받은 content가 null 이거나 내용이 없는 경우에도 파일을 생성할 것인지에 대한 옵션 (기본값: false)
     * */
    private boolean allowCreateEmptyFile = false;

    /**
     * directoryType 속성에 따라 <code>FileTemplate</code>에서 어떤 속성의 값을 생성한 파일의 저장 경로로써 사용할 지 결정된다.<br><br>
     * -기본값: <code>LOCAL_MOVE</code><br>
     * -REMOTE_SEND → <code>FileTemplate#remoteSendDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_RECEIVE → <code>FileTemplate#remoteReceiveDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_TEMP → <code>FileTemplate#remoteTempDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_SUCCESS → <code>FileTemplate#remoteSuccessDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_ERROR → <code>FileTemplate#remoteErrorDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_BACKUP → <code>FileTemplate#remoteBackupDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_MOVE → <code>FileTemplate#remoteMoveDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_COPY → <code>FileTemplate#remoteCopyDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_SEND → <code>FileTemplate#localSendDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_RECEIVE → <code>FileTemplate#localReceiveDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_TEMP → <code>FileTemplate#localTempDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_SUCCESS → <code>FileTemplate#localSuccessDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_ERROR → <code>FileTemplate#localErrorDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_BACKUP → <code>FileTemplate#localBackupDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_MOVE → <code>FileTemplate#localMoveDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_COPY → <code>FileTemplate#localCopyDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * */
    private DirectoryType directoryType = DirectoryType.LOCAL_MOVE;

    @Override
    public void process(ServiceContext ctx) throws Throwable {

    }

}
