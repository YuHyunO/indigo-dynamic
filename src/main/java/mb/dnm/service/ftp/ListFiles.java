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
import org.eclipse.jetty.util.IO;

import java.io.IOException;
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
    private String pathSeparator;
    /**
     * 기본값: true<br>
     * 파일 목록을 탐색할 경로가 디렉터리인 경우 그 디렉터리의 하부 파일들을 재귀적으로 탐색할 지에 대한 여부를 결정한다.
     * <code>type</code> 속성이 ALL 또는 DIRECTORY 인 경우에만 유효하다.
     * <code>type</code> 속성이 DIRECTORY 인 경우에 이 속성을 사용하면 파일목록을 가져올 경로로 지정한 최상위 경로에서는 디렉터리만 탐색을 하고,
     * 다시 그 디렉터리의 하위를 탐색할 때는 파일과 디렉터리 구분에 대한 필터링이 적용되지 않는다.
     * */
    private boolean searchRecursively = true;

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
        if (pathSeparator == null) {
            ftp.changeWorkingDirectory("");
            pathSeparator = ftp.printWorkingDirectory();
        }

        List<String> filePathList = new ArrayList<>();

        if (!ftp.changeWorkingDirectory(targetPath)) {
            log.warn("[{}]Can not change directory to '{}'. No directory exists having that name or no permission.", ctx.getTxId(), targetPath);
        }

        FTPFile[] files = ftp.listFiles();
        String workingDir = ftp.printWorkingDirectory();
        if (!workingDir.endsWith(pathSeparator)) {
            workingDir += pathSeparator;
        }

        for (FTPFile file : files) {
            String fileName = file.getName();

            if (filter.accept(fileName)) {
                String fullPath = workingDir + file.getName();
                if (tmpType == FileType.DIRECTORY) {
                    if (!file.isDirectory())
                        continue;
                    filePathList.add(fullPath);
                    if (searchRecursively) {
                        filePathList.addAll(searchRecursively(ftp, fullPath));
                    }

                } else if (tmpType == FileType.FILE) {
                    if (file.isDirectory())
                        continue;
                    filePathList.add(fullPath);
                } else {
                    filePathList.add(fullPath);
                    if (file.isDirectory() && searchRecursively) {
                        filePathList.addAll(searchRecursively(ftp, fullPath));
                    }
                }
            }

        }

        setOutputValue(ctx, filePathList);
    }

    protected List<String> searchRecursively(FTPClient ftp, String dirName) throws IOException {
        List<String> innerFiles = new ArrayList<>();
        FTPFile[] files = {};
        files = ftp.listFiles(dirName);
        if (files.length > 0) {
            for (FTPFile file : files) {
                String pathName = dirName
                        + (dirName.equals(pathSeparator) ? "" : pathSeparator)
                        + file.getName();
                if (file.isDirectory()) {
                    innerFiles.addAll(searchRecursively(ftp, pathName));
                } else {
                    innerFiles.add(pathName);
                }
            }
        }

        return innerFiles;
    }

}
