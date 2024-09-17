package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileList;
import mb.dnm.code.DirectoryType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;

import java.io.File;
import java.nio.file.*;
import java.util.*;

/**
 * 파일 Storage 의 파일을 복사한다.
 * 어느 경로로 파일을 복사할 지에 대한 정보는 <code>InterfaceInfo</code> 에 저장된 <code>FileTemplate</code> 의 속성들로부터 가져온다.
 *
 * @see ListFiles
 * @see FileList
 *
 * @author Yuhyun O
 * @version 2024.09.16
 *
 * @Input 복사할 파일의 경로
 * @InputType <code>String</code>(1건) 또는 <code>List&lt;String&gt;</code> 또는 <code>Set&lt;String&gt;</code> 또는 <code>FileList</code><br>
 * input이 List로 전달되는 경우 중복된 경로가 존재하더라도 내부적으로 Set 객체에 다시 담기게 되므로 중복값이 제거된다.
 * @Output 복사한 파일의 복사 후 경로 리스트
 * @OutputType <code>List&lt;String&gt;</code>
 * @ErrorOutput 파일을 복사 하는 중 에러가 발생하여 복사에 실패하는 경우 에러가 발생한 파일의 경로
 * @ErrorOutputType <code>List&lt;String&gt;</code>
 * */
@Slf4j
@Setter
public class CopyFiles extends SourceAccessService {
    /**
     * directoryType 속성에 따라 <code>FileTemplate</code>에서 어떤 속성의 값을 목록을 복사할 경로로써 사용할 지 결정된다.<br><br>
     * -기본값: <code>LOCAL_COPY</code><br>
     * -REMOTE_SEND → <code>FileTemplate#remoteSendDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -REMOTE_RECEIVE → <code>FileTemplate#remoteReceiveDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -REMOTE_TEMP → <code>FileTemplate#remoteTempDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -REMOTE_SUCCESS → <code>FileTemplate#remoteSuccessDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -REMOTE_ERROR → <code>FileTemplate#remoteErrorDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -REMOTE_BACKUP → <code>FileTemplate#remoteBackupDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -REMOTE_MOVE → <code>FileTemplate#remoteMoveDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -REMOTE_COPY → <code>FileTemplate#remoteCopyDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -LOCAL_SEND → <code>FileTemplate#localSendDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -LOCAL_RECEIVE → <code>FileTemplate#localReceiveDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -LOCAL_TEMP → <code>FileTemplate#localTempDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -LOCAL_SUCCESS → <code>FileTemplate#localSuccessDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -LOCAL_ERROR → <code>FileTemplate#localErrorDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -LOCAL_BACKUP → <code>FileTemplate#localBackupDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -LOCAL_MOVE → <code>FileTemplate#localMoveDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * -LOCAL_COPY → <code>FileTemplate#localCopyDir</code> 을 파일목록을 복사할 경로로 사용함<br>
     * */
    private DirectoryType directoryType = DirectoryType.LOCAL_COPY;
    /**
     * 기본값: false
     * */
    private boolean ignoreErrorFile = false;
    private boolean debuggingWhenCopied = true;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "CopyFiles service must have the input parameter in which contain the files to copy");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = info.getSourceNameByAlias(getSourceAlias());
        String txId = ctx.getTxId();

        //input으로 전달된 복사 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);
        List<String> targetFilePaths = new ArrayList<>();
        if (inputVal == null) {
            log.debug("The value of input '{}' is not found. No file paths to copy found in context data.", getInput());
            return;
        }

        /*파일을 복사 할 때 원본 디렉터리 구조 그대로 복사 할 지 판단하는 flag이다.
        input 데이터 타입이 FileList인 경우에만 적용된다.
        */
        boolean saveStructureAsIs = false;
        String baseDir = null;
        try {
            if (inputVal instanceof FileList) {
                FileList fileList = (FileList) inputVal;
                targetFilePaths = fileList.getFileList();
                saveStructureAsIs = true;
                baseDir = fileList.getBaseDirectory();

            } else if (inputVal instanceof String) {
                targetFilePaths.add((String) inputVal);

            } else if (inputVal instanceof List) {
                Set<String> tmpSet = new HashSet<>((List<String>) inputVal);
                if (tmpSet.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to copy found in context data.", getInput());
                    return;
                }
                targetFilePaths.addAll(tmpSet);
            } else if (inputVal instanceof Set) {
                Set<String> tmpSet = new HashSet<>((Set<String>) inputVal);
                if (tmpSet.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to copy found in context data.", getInput());
                    return;
                }
                targetFilePaths.addAll(tmpSet);
            } else {
                throw new ClassCastException();
            }
        } catch (ClassCastException ce) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The type of the input parameter value is not String or List<String> or Set<String> or FileList. Inputted value's type: " + inputVal.getClass().getName());
        }

        List<String> movedFileList = new ArrayList<>();
        List<String> errorFilePaths = new ArrayList<>();
        int inputListSize = targetFilePaths.size();
        int dirCount = 0;
        int successCount = 0;
        int notExistInSource = 0;

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

        //복사할 파일이 디렉터리인 경우 디렉터리 하부 내용을 먼저 옮긴 뒤 디렉터리를 가장 마지막에 복사하기 위해 따로 저장한다.
        Map<Path, Path> pathsToCopiedLast = new HashMap<>();
        log.info("[{}]Copying files ...", txId);
        for (String oldFilePathStr : targetFilePaths) {
            Path oldPath = null;
            Path pathToCp = null;
            boolean dirFlag = false;

            //saveStructureAsIs가 true인 경우 즉, input 객체로 FileList가 전달된 경우 본래의 디렉터리 구조를 그대로 하여 파일을 복사 하기 위해 복사할 파일 경로에도 동일한 디렉터리 구조를 만드는 과정이다.
            if (saveStructureAsIs) {
                StringBuffer dirToMadeBf = new StringBuffer();
                dirToMadeBf.append(savePath)
                        .append(savePath.endsWith(File.separator) ? "" : File.separator);
                dirToMadeBf.append(oldFilePathStr);

                Path dirToMade = Paths.get(dirToMadeBf.toString());
                if (dirToMadeBf.charAt(dirToMadeBf.length() - 1) != File.separatorChar) {//파일인 경우 dirToMade에 그 상위 디렉터리를 지정한다.
                    dirToMade = dirToMade.getParent();
                    pathToCp = Paths.get(dirToMade.toString(), new File(oldFilePathStr).getName());
                } else {
                    dirFlag = true;
                    pathToCp = dirToMade;
                }

                if (!Files.exists(dirToMade)) {
                    Files.createDirectories(dirToMade);
                }
                oldPath = Paths.get(baseDir, oldFilePathStr);

            } else {
                dirFlag = oldFilePathStr.endsWith(File.separator);
                oldPath = Paths.get(oldFilePathStr);
                pathToCp = Paths.get(savePath, new File(oldFilePathStr).getName());
            }

            try {
                //위에서 saveStructureAsIs가 true 인 경우 필요한 디렉터리들을 만들었다면 이 부분은 빈 파일을 생성하는 과정이다.
                if (!Files.exists(pathToCp)) {
                    Files.createFile(pathToCp);
                }
                
                if (!dirFlag) {
                    if (Files.exists(oldPath)) {
                        Path copied = Files.copy(oldPath, pathToCp, StandardCopyOption.REPLACE_EXISTING);
                        movedFileList.add(copied.toString());
                        ++successCount;
                    } else {
                        ++notExistInSource;
                    }
                } else {
                    pathsToCopiedLast.put(oldPath, pathToCp);
                    ++dirCount;
                }
                if (debuggingWhenCopied) {
                    log.debug("[{}]File copy success. Old path: \"{}\", copied path: \"{}\"", txId, oldPath, pathToCp);
                }

            } catch (Throwable t) {
                if (ignoreErrorFile) {
                    errorFilePaths.add(oldPath.toString());
                    log.warn("[{}]Exception occurred when moving file but ignored. Cause: {}", txId, MessageUtil.toString(t));
                } else {
                    throw t;
                }
            }
        }

        if (!pathsToCopiedLast.isEmpty()) {

            //DirectoryNotEmptyException을 방지하기 위해 최하위 파일부터 복사할 수 있도록 정렬한다.
            List<Path> sortingList = new ArrayList<>(pathsToCopiedLast.keySet());
            Collections.sort(sortingList, new Comparator<Path>() {
                @Override
                public int compare(Path o1, Path o2) {
                    return (o2.toString()).compareToIgnoreCase(o1.toString());
                }
            });

            for (Path oldPath : sortingList) {
                Path pathToCp = pathsToCopiedLast.get(oldPath);
                try {
                    try {
                        if (!Files.exists(pathToCp)) {
                            Files.copy(oldPath, pathToCp, StandardCopyOption.REPLACE_EXISTING);
                        }
                        ++successCount;
                        if (debuggingWhenCopied) {
                            log.debug("[{}]Directory copy success. Old path: \"{}\", copied path: \"{}\"", txId, oldPath, pathToCp);
                        }
                    } catch (DirectoryNotEmptyException de) {}
                } catch (Throwable t) {
                    if (ignoreErrorFile) {
                        errorFilePaths.add(oldPath.toString());
                        log.warn("[{}]Exception occurred when moving file but ignored. Cause: {}", txId, MessageUtil.toString(t));
                    } else {
                        throw t;
                    }
                }
            }
        }


        if (getOutput() != null) {
            setOutputValue(ctx, movedFileList);
        }
        if (getErrorOutput() != null) {
            if (!errorFilePaths.isEmpty()) {
                setErrorOutputValue(ctx, errorFilePaths);
            }
        }
        log.info("[{}]File copy result: inputFileList[file={} / directory={}], copy_success={}, not_exist_in_source={}, error_count={}"
                , txId, inputListSize - dirCount, dirCount, successCount, notExistInSource, errorFilePaths.size());

    }

}
