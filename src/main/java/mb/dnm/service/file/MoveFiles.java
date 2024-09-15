package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileList;
import mb.dnm.code.DirectoryType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.FileTemplate;
import mb.dnm.storage.InterfaceInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 어느 경로로 파일을 이동할 지에 대한 정보는 <code>InterfaceInfo</code> 에 저장된 <code>FileTemplate</code> 의 속성들로부터 가져온다.
 *
 * @see mb.dnm.service.file.ListFiles
 * @see mb.dnm.access.file.FileList
 *
 * @author Yuhyun O
 * @version 2024.09.15
 *
 * @Input 이동할 받을 파일의 경로
 * @InputType <code>String</code>(1건) 또는 <code>List&lt;String&gt;</code>(여러건) 또는 <code>FileList</code>
 * @Output 이동한 파일의 이동 후 경로 리스트
 * @OutputType <code>List&lt;String&gt;</code>
 * @ErrorOutput 파일을 다운로드 하는 중 에러가 발생하여 다운로드에 실패하는 경우 에러가 발생한 파일의 경로
 * @ErrorOutputType <code>List&lt;String&gt;</code>
 * */
@Slf4j
@Setter
public class MoveFiles extends SourceAccessService {
    /**
     * directoryType 속성에 따라 <code>FileTemplate</code>에서 어떤 속성의 값을 목록을 가져올 경로로써 사용할 지 결정된다.<br><br>
     * -기본값: <code>LOCAL_BACKUP</code><br>
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
    private DirectoryType directoryType = DirectoryType.LOCAL_BACKUP;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "MoveFiles service must have the input parameter in which contain the files to move");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = info.getSourceNameByAlias(getSourceAlias());
        String txId = ctx.getTxId();

        //input으로 전달된 다운로드 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);
        List<String> targetFilePaths = new ArrayList<>();
        if (inputVal == null) {
            log.debug("The value of input '{}' is not found. No file paths to move found in context data.", getInput());
            return;
        }

        /*파일을 이동 할 때 FTP 서버의 디렉터리 구조 그대로 다운로드 할 지 판단하는 flag이다.
        input 데이터 타입이 FileList인 경우에만 적용된다.
        */
        boolean saveStructureAsIs = false;
        try {
            if (inputVal instanceof FileList) {
                FileList fileList = (FileList) inputVal;
                targetFilePaths = fileList.getFileList();
                saveStructureAsIs = true;

            } else if (inputVal instanceof String) {
                targetFilePaths.add((String) inputVal);

            } else if (inputVal instanceof List) {
                List<String> tmpList = (List<String>) inputVal;
                if (tmpList.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to download found in context data.", getInput());
                    return;
                }
                targetFilePaths.addAll(tmpList);
            } else {
                throw new ClassCastException();
            }
        } catch (ClassCastException ce) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The type of the input parameter value is not String or List<String>. Inputted value's type: " + inputVal.getClass().getName());
        }

        List<String> errorFilePaths = new ArrayList<>();
        int inputListSize = targetFilePaths.size();
        int dirCount = 0;
        int successCount = 0;

        // outPutDataType 이 dataTypeDataType.FILE 인 경우 InterfaceInfo에서 FileTemplate을 가져와 directoryType 과 일치하는 경로에 파일을 저장함
        FileTemplate template = info.getFileTemplate(srcName);
        String savePath = null;
        if (template == null)
            throw new InvalidServiceConfigurationException(this.getClass(), "The File template with name '" + srcName + "' of the interface '" + info.getInterfaceId() + "' is null.");
        savePath = template.getFilePath(directoryType);
        if (savePath == null)
            throw new InvalidServiceConfigurationException(this.getClass(), "The value of " + directoryType + " of the template with name '" + srcName + "' is null");
        if (savePath.contains("@{if_id}")) {
            savePath = savePath.replace("@{if_id}", ctx.getInterfaceId());
        }
        //### PlaceHolder mapping 을 적용할 것

        Path path = Paths.get(savePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }


        log.info("[{}]FTP file download result: inputFileList[file={} / directory={}], move_success={}, error_count={}"
                , txId, inputListSize - dirCount, dirCount, successCount, errorFilePaths.size());

    }

}
