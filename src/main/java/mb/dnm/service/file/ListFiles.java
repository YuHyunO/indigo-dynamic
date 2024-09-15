package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileList;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.storage.InterfaceInfo;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 접근 가능한 디스크의 파일 또는 디렉터리 목록을 가져온다.
 * 어느 경로의 어떤 파일 목록을 가져올 지에 대한 정보는 <code>InterfaceInfo</code> 에 저장된 <code>FileTemplate</code> 의 속성들로부터 가져온다.
 *
 * @see mb.dnm.access.file.FileList
 *
 * @author Yuhyun O
 * @version 2024.09.15
 *
 * @Input List를 가져올 Directory의 경로
 * @InputType <code>String</code>
 * @Output File 또는 Directory 경로
 * @OutputType <code>FileList</code>
 * */

@Slf4j
@Setter
public class ListFiles extends SourceAccessService {
    /**
     * directoryType 속성에 따라 <code>FileTemplate</code>에서 어떤 속성의 값을 목록을 가져올 경로로써 사용할 지 결정된다.<br><br>
     * -기본값: <code>LOCAL_SEND</code><br>
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
    private DirectoryType directoryType = DirectoryType.LOCAL_SEND;
    private String listDirectory;
    private String fileNamePattern = "*";
    private FileType type = FileType.ALL;
    /**
     * 기본값: false<br>
     * 파일 목록을 탐색할 경로가 디렉터리인 경우 그 디렉터리의 하부 파일들을 재귀적으로 탐색할 지에 대한 여부를 결정한다.
     * <code>type</code> 속성이 ALL 또는 DIRECTORY 인 경우에만 유효하다.
     * <code>type</code> 속성이 DIRECTORY 인 경우에 이 속성을 사용하면 파일목록을 가져올 경로로 지정한 최상위 경로에서는 디렉터리만 탐색을 하고,
     * 다시 그 디렉터리의 하위를 탐색할 때는 파일과 디렉터리 구분에 대한 필터링이 적용되지 않는다.
     * */
    private boolean searchRecursively = false;
    /**
     * 목록을 탐색하려는 경로로 지정된 디렉터리가 존재하지 않는 경우 새로 생성하는 옵션
     * */
    private boolean createDirectoriesWhenNotExist = false;


    @Override
    public void process(ServiceContext ctx) throws Throwable {
        InterfaceInfo info = ctx.getInfo();
        String srcName = info.getSourceNameByAlias(getSourceAlias());
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

        if (targetPath.contains("@{if_id}")) {
            targetPath = targetPath.replace("@{if_id}", ctx.getInterfaceId());
        }

        if (!targetPath.endsWith(File.separator)) {
            targetPath = targetPath + File.separator;
        }
        Path path = Paths.get(targetPath);


        //파일목록을 탐색하려는 경로가 존재하지 않는 경우 처리
        if (!Files.exists(path)) {
            if (createDirectoriesWhenNotExist) {
                Path baseDir = Files.createDirectories(path);
                log.debug("[{}]The directory '{}' is created just now and so no files exist", ctx.getTxId(), baseDir.toFile());
            } else {
                log.info("[{}]The directory '{}' is not exist.", ctx.getTxId(), targetPath);
            }
            return;
        }

        FileList fileList = new FileList();
        List<String> searchedFileList = new ArrayList<>();
        fileList.setBaseDirectory(targetPath);

        File[] files = path.toFile().listFiles((FileFilter) new WildcardFileFilter(tmpFileNamePattern));
        int baseDirIdx = 0;
        for (File file : files) {
            if (baseDirIdx == 0) {
                baseDirIdx = file.toString().indexOf(targetPath) + targetPath.length();
            }
            String filePathAfterBaseDir = file.toString().substring(baseDirIdx);

            //아래에 FileType이 DIRECTORY 이냐 FILE 이냐 또는 ALL 이냐에 따라 반복되는 코드는 굳이 정리하지 않았음
            if (tmpType == FileType.DIRECTORY) {
                if (file.isDirectory()) {
                    if (!filePathAfterBaseDir.endsWith(File.separator)) {
                        filePathAfterBaseDir += File.separator;
                    }
                    searchedFileList.add(filePathAfterBaseDir);
                    if (searchRecursively) {
                        searchedFileList.addAll(searchRecursively(targetPath, file));
                    }
                }
            } else if (tmpType == FileType.FILE) {
                if (file.isFile()) {
                    if (filePathAfterBaseDir.startsWith(File.separator)) {
                        filePathAfterBaseDir = filePathAfterBaseDir.substring(1);
                    }
                    searchedFileList.add(filePathAfterBaseDir);
                }
            } else {
                if (file.isDirectory()) {
                    if (!filePathAfterBaseDir.endsWith(File.separator)) {
                        filePathAfterBaseDir += File.separator;
                    }
                    searchedFileList.add(filePathAfterBaseDir);
                    if (searchRecursively) {
                        searchedFileList.addAll(searchRecursively(targetPath, file));
                    }
                } else {
                    if (filePathAfterBaseDir.startsWith(File.separator)) {
                        filePathAfterBaseDir = filePathAfterBaseDir.substring(1);
                    }
                    searchedFileList.add(filePathAfterBaseDir);
                }
            }
        }
        log.info("[{}] {} files found in the path \"{}\".", ctx.getTxId(), searchedFileList.size(), targetPath);
        fileList.setFileList(searchedFileList);
        setOutputValue(ctx, fileList);
    }

    /**
     * 디렉터리를 재귀적으로 탐색하여 모든 파일 목록을 가져오는 메소드
     *
     * @param baseDir 파일 목록을 탐색하려는 최상위 경로 즉, 파일 목록을 가져오기로 설정된 경로.
     * @param subDir 재귀적으로 탐색하고자 하는 디렉터리 경로
     *
     * */
    private List<String> searchRecursively(String baseDir, File subDir) throws IOException {
        List<String> innerFiles = new ArrayList<>();

        File[] files = subDir.listFiles();
        int baseDirIdx = 0;
        if (files != null) {
            for (File file : files) {
                if (baseDirIdx == 0) {
                    baseDirIdx = file.toString().indexOf(baseDir) + baseDir.length();
                }
                /*filePathAfterBaseDir -> baseDir 이후에 탐색된 디렉터리 또는 파일의 경로를 의미한다.
                 현재 반복문을 통해 리스트에서 추출되는 File 객체가 디렉터리인 경우 filePathAfterBaseDir 을
                 searchRecursively(String baseDir, File subDir) 메소드의 subDir 파라미터로 전달하여 재귀호출한다.
                */
                String filePathAfterBaseDir = file.toString().substring(baseDirIdx);
                if (file.isDirectory()) {
                    /*이 메소드를 통해 리턴되는 파일경로 리스트의 값들은 FileList 객체에 담기게 된다.
                     FileList 객체를 통해 파일목록을 가져와 작업을 하는 경우 각 원소가 디렉터리인지 파일인지 구분할 수 없으므로
                     디렉터리는 경로명 끝에 파일 구분자를 더하고 파일은 구분자 없이 목록에 추가한다.
                    */
                    if (!filePathAfterBaseDir.endsWith(File.separator))
                        filePathAfterBaseDir += File.separator;
                    innerFiles.add(filePathAfterBaseDir);
                    innerFiles.addAll(searchRecursively(baseDir, file));
                } else {
                    innerFiles.add(filePathAfterBaseDir);
                }
            }
        }

        return innerFiles;
    }
}
