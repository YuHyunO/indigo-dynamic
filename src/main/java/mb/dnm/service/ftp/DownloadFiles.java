package mb.dnm.service.ftp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.code.DataType;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.FileTemplate;
import mb.dnm.storage.InterfaceInfo;
import org.apache.commons.net.ftp.FTPClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Input으로 전달받은 파일 경로로부터 파일의 데이터를 다운로드한다.<br>
 * dataType 속성이 FILE인 경우, 지정된 경로로 파일이 저장되며 다운로드 받은 File이 저장된 경로가 리스트로 output 된다.<br> dataType 속성이 BYTE_ARRAY 인 경우,
 * 파일명과 파일의 데이터가 <code>Map&lt;String, byte[]&gt;</code> 형태로 담긴 리스트가 output 된다.<br>
 * FTP 파일의 데이터가 파일로 저장되도록 설정된 경우, 어느 경로에 파일을 저장할 지에 대한 정보는 <code>InterfaceInfo</code> 에 저장된 <code>FileTemplate</code> 의 속성들로부터 가져온다.<br>
 * 보통 <code>mb.dnm.service.ftp.ListFiles</code> 서비스와 연계해서 사용한다.
 *
 * @see mb.dnm.service.ftp.ListFiles
 *
 * @author Yuhyun O
 * @version 2024.09.12
 *
 * @Input 다운로드 받을 FTP서버의 파일경로
 * @InputType <code>String</code>(1건) 또는 <code>List&lt;String&gt;</code>(여러건)
 * @Output outPutDataType 속성이 FILE인 경우, 지정된 경로로 파일이 저장되며 다운로드 받은 File이 저장된 경로가 리스트로 output 된다.<br> outPutDataType 속성이 BYTE_ARRAY 인 경우,
 * 파일명과 파일의 데이터가 <code>Map&lt;String, byte[]&gt;</code> 형태로 담긴 리스트가 output 된다.
 * @OutputType dataType(FILE): <code>List&lt;String&gt;</code>, dataType(BYTE_ARRAY): <code>List&lt;Map&lt;String, byte[]&gt;&gt;</code>
 * */
@Slf4j
@Setter
public class DownloadFiles extends AbstractFTPService {

    /**
     * outPutDataType 속성에 따라 파일의 데이터가 어떤 식으로 저장될 지 결정된다.<br>
     * -FILE (default) → <code>FileTemplate</code> 에서 directoryType과 일치하는 경로정보를 가져와서에서 파일로 저장된다.
     * -BYTE_ARRAY →
     * */
    private DataType outPutDataType = DataType.FILE;

    /**
     * outPutDataType 속성이 DataType.FILE 인 경우에만 이 속성이 유효하다.<br>
     * directoryType 속성에 따라 <code>FileTemplate</code>에서 어떤 속성의 값을 목록을 가져올 경로로써 사용할 지 결정된다.<br><br>
     * -기본값: <code>LOCAL_RECEIVE</code><br>
     * -REMOTE_SEND → <code>FileTemplate#remoteSendDir</code> 에 파일이 저장됨<br>
     * -REMOTE_RECEIVE → <code>FileTemplate#remoteReceiveDir</code> 에 파일이 저장됨<br>
     * -REMOTE_TEMP → <code>FileTemplate#remoteTempDir</code> 에 파일이 저장됨<br>
     * -REMOTE_SUCCESS → <code>FileTemplate#remoteSuccessDir</code> 에 파일이 저장됨<br>
     * -REMOTE_ERROR → <code>FileTemplate#remoteErrorDir</code> 에 파일이 저장됨<br>
     * -REMOTE_BACKUP → <code>FileTemplate#remoteBackupDir</code> 에 파일이 저장됨<br>
     * -LOCAL_SEND → <code>FileTemplate#localSendDir</code> 에 파일이 저장됨<br>
     * -LOCAL_RECEIVE → <code>FileTemplate#localReceiveDir</code> 에 파일이 저장됨<br>
     * -LOCAL_TEMP → <code>FileTemplate#localTempDir</code> 에 파일이 저장됨<br>
     * -LOCAL_SUCCESS → <code>FileTemplate#localSuccessDir</code> 에 파일이 저장됨<br>
     * -LOCAL_ERROR → <code>FileTemplate#localErrorDir</code> 에 파일이 저장됨<br>
     * -LOCAL_BACKUP → <code>FileTemplate#localBackupDir</code> 에 파일이 저장됨<br>
     * */
    private DirectoryType directoryType = DirectoryType.LOCAL_RECEIVE;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "DownloadFiles service must have the input parameter in which contain");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = getFTPSourceName(info);

        //input으로 전달된 다운로드 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);
        List<String> targetFileNames = new ArrayList<>();
        if (inputVal == null) {
            log.debug("The value of input '{}' is not found. No list of file path to download found in context data.", getInput());
            return;
        }

        try {
            if (inputVal instanceof String) {
                targetFileNames.add((String) inputVal);
            } else if (inputVal instanceof List) {
                List<String> tmpList = (List<String>) inputVal;
                if (tmpList.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to download found in context data.", getInput());
                    return;
                }
                targetFileNames.addAll(tmpList);
            } else {
                throw new ClassCastException();
            }
        } catch (ClassCastException ce) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The type of the input parameter value is not String or List<String>. Inputted value's type: " + inputVal.getClass().getName());
        }


        FTPSession session = (FTPSession) ctx.getSession(srcName);
        if (session == null) {
            new FTPLogin().process(ctx);
        }
        FTPClient ftp = session.getFTPClient();

        if (outPutDataType == DataType.FILE) {
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

            Path path = Paths.get(savePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }




        } else if (outPutDataType == DataType.BYTE_ARRAY) {
            // outPutDataType 이 dataTypeDataType.FILE 인 경우 InterfaceInfo에서 FileTemplate을 가져와 directoryType 과 일치하는 경로에 파일을 저장함


        }

    }

    public void setOutPutDataType(DataType outPutDataType) {
        if (outPutDataType != DataType.FILE || outPutDataType != DataType.BYTE_ARRAY) {
            throw new IllegalArgumentException("Not supported data type: " + outPutDataType);
        }
        this.outPutDataType = outPutDataType;
    }

}
