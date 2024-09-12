package mb.dnm.service.ftp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileNamePatternFilter;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.storage.FileTemplate;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.FileUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;
import java.util.List;


/**
 * FTP 서버의 파일 또는 디렉터리 목록을 가져온다.
 * 어느 경로의 어떤 파일 목록을 가져올 지에 대한 정보는 <code>InterfaceInfo</code> 에 저장된 <code>FileTemplate</code> 의 속성들로부터 가져온다.
 *
 * @see mb.dnm.service.ftp.FTPLogin
 *
 * @author Yuhyun O
 * @version 2024.09.10
 *
 * @Input List를 가져올 Directory의 경로
 * @InputType <code>String</code>
 * @Output File 또는 Directory 경로
 * @OutputType <code>List&lt;String&gt;</code>
 * */

@Slf4j
@Setter
public class ListFiles extends AbstractFTPService {
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
    private char fileSeparator = '/';

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        InterfaceInfo info = ctx.getInfo();
        String srcName = getFTPSourceName(info);
        String tmpFileNamePattern = fileNamePattern;
        FileType tmpType = type;

        String targetPath = null;
        
        
        /*
        * 파일 목록을 가져올 때 어느 경로에서 가져올 지에 대한 우선순위
        * 1. GetFileList 서비스에 등록된 listDirectory를 사용
        * 2. 1번이 없는 경우 GetFileList 서비스에 지정된 Input 파라미터의 값을 사용
        * 3. 1, 2번 모두 해당사항이 없는 경우 InterfaceInfo를 통해 가져온 FileTemplate의 정보를 사용
        * */
        if (listDirectory != null) {
            targetPath = listDirectory;
        } else if (getInput() != null) {
            Object temp = getInputValue(ctx);
            if (temp == null)
                throw new InvalidServiceConfigurationException(this.getClass(), "The input parameter name'" + getInput() + "' of this service is registered. But the value is null.");
            try {
                targetPath = (String) temp;
            } catch (ClassCastException ce) {
                throw new InvalidServiceConfigurationException(this.getClass(), "The input parameter's value of this service must be String.class. But the value given is '" + temp.getClass() + "'");
            }
        } else {
            //FileTemplate을 InterfaceInfo에서 가져올 때 FTPClientTemplate의 templateName과 일치하는 것을 가져옴
            FileTemplate template = info.getFileTemplate(srcName);
            if (template == null)
                throw new InvalidServiceConfigurationException(this.getClass(), "The File template with name '" + srcName + "' of the interface '" + info.getInterfaceId() + "' is null.");
            targetPath = template.getFilePath(directoryType);
            if (targetPath == null)
                throw new InvalidServiceConfigurationException(this.getClass(), "The value of " + directoryType + " of the template with name '" + srcName + "' is null");
            tmpFileNamePattern = template.getFileNamePattern();
            tmpType = template.getType();
        }

        FTPSession session = (FTPSession) ctx.getSession(srcName);
        if (session == null) {
            new FTPLogin().process(ctx);
        }
        if (targetPath.contains("@{if_id}")) {
            targetPath = targetPath.replace("@{if_id}", ctx.getInterfaceId());
        }
        targetPath = FileUtil.removeLastPathSeparator(targetPath);

        FileNamePatternFilter filter = new FileNamePatternFilter(tmpFileNamePattern);
        FTPClient ftp = session.getFTPClient();

        List<String> filePathList = new ArrayList<>();
        FTPFile[] files = ftp.listFiles(targetPath);

        String fileSeparator = FileUtil.supposeFileSeparator(targetPath);
        String tmpTargetPath = targetPath;

        if (targetPath.length() != 1) {
            tmpTargetPath = targetPath + fileSeparator;
        }

        for (FTPFile file : files) {
            String fileName = file.getName();

            if (filter.accept(fileName)) {
                if (tmpType == FileType.DIRECTORY) {
                    if (!file.isDirectory())
                        continue;
                    filePathList.add(tmpTargetPath + file.getName());
                } else if (tmpType == FileType.FILE) {
                    if (file.isDirectory())
                        continue;
                    filePathList.add(tmpTargetPath + file.getName());
                } else {
                    filePathList.add(tmpTargetPath + file.getName());
                }
            }

        }

        setOutputValue(ctx, filePathList);
    }


}
