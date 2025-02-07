package mb.dnm.service.ftp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileList;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.FileUtil;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * FTP 서버의 파일 또는 디렉터리 목록을 가져온다.
 * 가져올 파일의 디렉터리, 파일명 패턴 등에 대한 정보는 {@code InterfaceInfo} 에 저장된 {@code FileTemplate} 의 속성들로부터 가져온다.
 * <br>
 * <br>
 * *<b>Input</b>: 목록을 가져올 디렉터리 경로<br>
 * *<b>Input type</b>: {@code String}
 * <br>
 * <br>
 * *<b>Output</b>: 파일 또는 디렉터리 목록<br>
 * *<b>Output type</b>: {@code FileList}
 * <br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.ftp.ListFiles"&gt;
 *     &lt;property name="sourceAlias"            value="<span style="color: black; background-color: #FAF3D4;">source alias</span>"/&gt;
 *     &lt;property name="directoryType"          value="<span style="color: black; background-color: #FAF3D4;">DirectoryType</span>"/&gt;
 *     &lt;property name="input"                  value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                 value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @see FTPLogin
 * @see mb.dnm.access.file.FileList
 * */

@Slf4j
@Setter
public class ListFiles extends AbstractFTPService implements Serializable {
    private static final long serialVersionUID = 6744130189101816379L;
    private DirectoryType directoryType = DirectoryType.REMOTE_SEND;
    private String listDirectory;
    private String fileNamePattern = "*";
    private FileType type = FileType.ALL;
    /**
     * 목록을 탐색하려는 경로로 지정된 디렉터리가 존재하지 않는 경우 새로 생성하는 옵션 (기본값: false)
     * */
    private boolean createDirectoriesWhenNotExist = false;
    /**
     * 기본값: false<br>
     * 파일 목록을 탐색할 경로가 디렉터리인 경우 그 디렉터리의 하부 파일들을 재귀적으로 탐색할 지에 대한 여부를 결정한다.
     * <code>type</code> 속성이 ALL 또는 DIRECTORY 인 경우에만 유효하다.
     * <code>type</code> 속성이 DIRECTORY 인 경우에 이 속성을 사용하면 파일목록을 가져올 경로로 지정한 최상위 경로에서는 디렉터리만 탐색을 하고,
     * 다시 그 디렉터리의 하위를 탐색할 때는 파일과 디렉터리 구분에 대한 필터링이 적용되지 않는다.
     * */
    private boolean searchRecursively = false;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        InterfaceInfo info = ctx.getInfo();
        String srcName = getFTPSourceName(info);
        String tmpFileNamePattern = fileNamePattern;
        FileType tmpType = type;

        String targetPath = null;
        
        
        /*
        * 파일 목록을 가져올 때 어느 경로에서 가져올 지에 대한 우선순위
        * 1. ListFiles 서비스에 등록된 listDirectory를 사용
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

        FTPSession session = getFTPSession(ctx, srcName);
        if (session == null) {
            new FTPLogin(getSourceAlias()).process(ctx);
            session = (FTPSession) ctx.getSession(srcName);
        }
        if (!session.isConnected()) {
            new FTPLogin(getSourceAlias()).process(ctx);
            session = (FTPSession) ctx.getSession(srcName);
        }

        /*if (targetPath.contains("@{if_id}")) {
            targetPath = targetPath.replace("@{if_id}", ctx.getInterfaceId());
        }*/
        //PlaceHolder mapping 을 적용할 것
        for (Map.Entry<String, Object> entry : ctx.getContextInformation().entrySet()) {
            StringBuilder keyBd = new StringBuilder(entry.getKey());
            String value = String.valueOf(entry.getValue());
            keyBd.deleteCharAt(0)
                    .insert(0, "@{")
                    .append("}");
            if (targetPath.contains(keyBd)) {
                targetPath = targetPath.replace(keyBd, value);
            }
        }

        targetPath = FileUtil.removeLastPathSeparator(targetPath);


        FTPClient ftp = session.getFTPClient();
        String pathSeparator = String.valueOf(ftp.printWorkingDirectory().charAt(0));
        if (pathSeparator.equals("null")) {
            pathSeparator = "/";
        }

        FileList fileList = new FileList();
        List<String> searchedFileList = new ArrayList<>();
        
        if (!ftp.changeWorkingDirectory(targetPath)) {
            String reply1 = ftp.getReplyString();
            boolean success = false;
            if (createDirectoriesWhenNotExist) {
                log.debug("[{}]Can not change directory to '{}'. Trying create the directory. Reply: {}", ctx.getTxId(), targetPath, reply1);
                log.info("[{}]Creating the directory '{}' ...", ctx.getTxId(), targetPath);
                if (ftp.makeDirectory(targetPath)) {
                    if (ftp.changeWorkingDirectory(targetPath)) {
                        success = true;
                    }
                }
            }

            if (!success) {
                String reply2 = ftp.getReplyString().trim();
                log.warn("[{}]Can not create and change directory '{}'. Reply: {}", ctx.getTxId(), targetPath, reply2);
                throw new InvalidServiceConfigurationException(this.getClass(), ftp.getReplyString().trim());
            }
        }

        FTPFile[] files = ftp.listFiles();
        String workingDir = ftp.printWorkingDirectory();

        if (!workingDir.endsWith(pathSeparator)) {
            workingDir += pathSeparator;
        }
        fileList.setBaseDirectory(workingDir);
        WildcardFileFilter filter = new WildcardFileFilter(tmpFileNamePattern);

        for (FTPFile file : files) {
            String fileName = file.getName();
            if (fileName.startsWith(pathSeparator))
                fileName = fileName.substring(pathSeparator.length());

            if (filter.accept(null, fileName)) {

                if (tmpType == FileType.DIRECTORY) {
                    if (!file.isDirectory())
                        continue;
                    if (!fileName.endsWith(pathSeparator))
                        fileName += pathSeparator;
                    searchedFileList.add(fileName);
                    if (searchRecursively) {
                        searchedFileList.addAll(searchRecursively(ftp, workingDir, fileName, pathSeparator));
                    }

                } else if (tmpType == FileType.FILE) {
                    if (file.isDirectory())
                        continue;
                    searchedFileList.add(fileName);

                } else {
                    if (file.isDirectory()) {
                        if (!fileName.endsWith(pathSeparator))
                            fileName += pathSeparator;
                        searchedFileList.add(fileName);
                        if (searchRecursively) {
                            searchedFileList.addAll(searchRecursively(ftp, workingDir, fileName, pathSeparator));
                        }
                    } else {
                        searchedFileList.add(fileName);
                    }
                }
            }

        }
        log.info("[{}] {} files found in the path \"{}\".", ctx.getTxId(), searchedFileList.size(), workingDir);
        fileList.setFileList(searchedFileList);
        setOutputValue(ctx, fileList);
    }

    /**
     * 디렉터리를 재귀적으로 탐색하여 모든 파일 목록을 가져오는 메소드
     *
     * @param ftp Connection이 맺어져 있는 FTPClient 객체
     * @param workingDir 파일 목록을 탐색하려는 최상위 경로 즉, 파일 목록을 가져오기로 설정된 경로.
     * @param dirName 재귀적으로 탐색하고자 하는 디렉터리 경로
     *                  
     * */
    private List<String> searchRecursively(FTPClient ftp, String workingDir, String dirName, String pathSeparator) throws IOException {
        List<String> innerFiles = new ArrayList<>();

        if (dirName.equals(pathSeparator)) {
            dirName = "";
        }
        if (dirName.startsWith(pathSeparator)) {
            dirName = dirName.substring(pathSeparator.length());
        }

        String searchPath = workingDir + dirName;

        FTPFile[] files = ftp.listFiles(searchPath);
        if (files != null) {
            for (FTPFile file : files) {
                String fileName = file.getName();
                if (fileName.startsWith(pathSeparator))
                    fileName = fileName.substring(pathSeparator.length());

                /*pathAfterWorkingDir -> workingDir 이후에 탐색된 디렉터리 또는 파일의 경로를 의미한다.
                 현재 반복문을 통해 리스트에서 추출되는 FTPFile 객체가 디렉터리인 경우 pathAfterWorkingDir 을
                 searchRecursively(FTPClient ftp, String workingDir, String dirName) 메소드의 dirName 파라미터로 전달하여 재귀호출한다.
                 */
                String pathAfterWorkingDir = dirName + fileName;
                if (file.isDirectory()) {
                    /*이 메소드를 통해 리턴되는 파일경로 리스트의 값들은 FileList 객체에 담기게 된다.
                    FileList 객체를 통해 파일목록을 가져와 작업을 하는 경우 각 원소가 디렉터리인지 파일인지 구분할 수 없으므로
                    디렉터리는 경로명 끝에 파일 구분자를 더하고 파일은 구분자 없이 목록에 추가한다.
                    */
                    if (!pathAfterWorkingDir.endsWith(pathSeparator))
                        pathAfterWorkingDir += pathSeparator;
                    innerFiles.add(pathAfterWorkingDir);
                    innerFiles.addAll(searchRecursively(ftp, workingDir, pathAfterWorkingDir, pathSeparator));
                } else {
                    innerFiles.add(pathAfterWorkingDir);
                }
            }
        }

        return innerFiles;
    }

}
