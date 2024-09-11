package mb.dnm.service.ftp;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileInfo;
import mb.dnm.access.file.FileNamePatternFilter;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.access.ftp.FTPSourceProvider;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.mapper.MesimPlaceHolderParam;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.FileTemplate;
import mb.dnm.storage.InterfaceInfo;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * FTP 서버의 파일 또는 디렉터리 목록을 가져온다.
 * 어느 경로의 어떤 파일 목록을 가져올 지에 대한 정보는 <code>InterfaceInfo</code> 에 저장된 <code>FileTemplate</code> 의 속성들로부터 가져온다.
 *
 *
 * @see mb.dnm.access.file.FileInfo
 * @see mb.dnm.service.ftp.FTPLogin
 *
 * @author Yuhyun O
 * @version 2024.09.10
 *
 * @Input List를 가져올 Directory의 경로
 * @OutputType <code>String</code>
 * @Output File 또는 Directory 정보
 * @OutputType <code>List&lt;FileInfo&gt;</code>
 * */

@Slf4j
public class GetFileList extends AbstractFTPService {
    private DirectoryType directoryType = DirectoryType.REMOTE_SEND;
    private String listDirectory;
    private String fileNamePattern;
    private FileType type = FileType.ALL;

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
        FileNamePatternFilter filter = new FileNamePatternFilter(tmpFileNamePattern);
        FTPClient ftp = session.getFTPClient();

        List<FileInfo> fileInfoList = new ArrayList<>();
        FTPFile[] files = ftp.listFiles(targetPath);
        for (FTPFile file : files) {
            String fileName = file.getName();
            if (filter.accept(fileName)) {
                if (tmpType == FileType.ALL || tmpType == FileType.DIRECTORY) {
                    if (!file.isDirectory())
                        continue;
                    fileInfoList.add(createFileInfo(file, targetPath));
                }
                if (tmpType == FileType.ALL || tmpType == FileType.FILE) {
                    if (!file.isDirectory())
                        continue;
                    fileInfoList.add(createFileInfo(file, targetPath));
                }
            }
        }

        setOutputValue(ctx, fileInfoList);
    }

    private FileInfo createFileInfo(FTPFile ftpFile, String parentDir) {
        FileInfo fileInfo = new FileInfo();
        

        return fileInfo;
    }

}
