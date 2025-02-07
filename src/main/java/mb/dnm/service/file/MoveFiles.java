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
import java.io.Serializable;
import java.nio.file.*;
import java.util.*;

/**
 * 파일을 이동한다.
 * 어느 경로로 파일을 이동할 지에 대한 정보는 {@link InterfaceInfo} 에 저장된 {@link FileTemplate} 의 속성들로부터 가져온다.
 * <br>
 * <br>
 * *<b>Input</b>: 이동할 파일의 경로<br>
 * *<b>Input type</b>: {@code String}, {@code List<String>}, {@code Set<String>}, {@link FileList}
 * <br>
 * <br>
 * *<b>Output</b>: 파일이 이동된 경로<br>
 * *<b>Output type</b>: {@code List<String>}
 * <br>
 * <br>
 * *<b>Error Output</b>: 이동 실패한 파일 경로<br>
 * *<b>Error Output type</b>: {@code List<String>}
 * <br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.file.MoveFiles"&gt;
 *     &lt;property name="sourceAlias"            value="<span style="color: black; background-color: #FAF3D4;">source alias</span>"/&gt;
 *     &lt;property name="directoryType"          value="<span style="color: black; background-color: #FAF3D4;">DirectoryType</span>"/&gt;
 *     &lt;property name="input"                  value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                 value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @see mb.dnm.service.file.ListFiles
 * @see mb.dnm.access.file.FileList
 * */
@Slf4j
@Setter
public class MoveFiles extends SourceAccessService implements Serializable {
    private static final long serialVersionUID = 4427626025280440315L;

    private DirectoryType directoryType = DirectoryType.LOCAL_MOVE;
    /**
     * 기본값: false
     * */
    private boolean ignoreErrorFile = false;
    private boolean debuggingWhenMoved = true;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "MoveFiles service must have the input parameter in which contain the files to move");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = info.getSourceNameByAlias(getSourceAlias());
        String txId = ctx.getTxId();

        //input으로 전달된 이동 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);
        List<String> targetFilePaths = new ArrayList<>();
        if (inputVal == null) {
            log.debug("[{}]The value of input '{}' is not found. No file paths to move found in context data.", txId, getInput());
            return;
        }

        /*파일을 이동 할 때 원본 디렉터리 구조 그대로 이동 할 지 판단하는 flag이다.
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
                    log.debug("The value of input '{}' is not found. No list of file path to move found in context data.", getInput());
                    return;
                }
                targetFilePaths.addAll(tmpSet);
            } else if (inputVal instanceof Set) {
                Set<String> tmpSet = new HashSet<>((Set<String>) inputVal);
                if (tmpSet.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to move found in context data.", getInput());
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
        /*if (savePath.contains("@{if_id}")) {
            savePath = savePath.replace("@{if_id}", ctx.getInterfaceId());
        }*/

        //### PlaceHolder mapping 을 적용할 것
        for (Map.Entry<String, Object> entry : ctx.getContextInformation().entrySet()) {
            StringBuilder keyBd = new StringBuilder(entry.getKey());
            String value = String.valueOf(entry.getValue());
            keyBd.deleteCharAt(0)
                    .insert(0, "@{")
                    .append("}");
            if (savePath.contains(keyBd)) {
                savePath = savePath.replace(keyBd, value);
            }
        }

        Path path = Paths.get(savePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        //이동할 파일이 디렉터리인 경우 디렉터리 하부 내용을 먼저 옮긴 뒤 디렉터리를 가장 마지막에 이동시키기 위해 따로 저장한다.
        Map<Path, Path> pathsToMovedLast = new HashMap<>();
        log.info("[{}]Moving files ...", txId);
        for (String oldFilePathStr : targetFilePaths) {
            Path oldPath = null;
            Path pathToMv = null;
            boolean dirFlag = false;

            //saveStructureAsIs가 true인 경우 즉, input 객체로 FileList가 전달된 경우 본래의 디렉터리 구조를 그대로 하여 파일을 이동 하기 위해 이동할 파일 경로에도 동일한 디렉터리 구조를 만드는 과정이다.
            if (saveStructureAsIs) {
                StringBuffer dirToMadeBf = new StringBuffer();
                dirToMadeBf.append(savePath)
                        .append(savePath.endsWith(File.separator) ? "" : File.separator);
                dirToMadeBf.append(oldFilePathStr);

                Path dirToMade = Paths.get(dirToMadeBf.toString());
                if (dirToMadeBf.charAt(dirToMadeBf.length() - 1) != File.separatorChar) {//파일인 경우 dirToMade에 그 상위 디렉터리를 지정한다.
                    dirToMade = dirToMade.getParent();
                    pathToMv = Paths.get(dirToMade.toString(), new File(oldFilePathStr).getName());
                } else {
                    dirFlag = true;
                    pathToMv = dirToMade;
                }

                if (!Files.exists(dirToMade)) {
                    Files.createDirectories(dirToMade);
                }
                oldPath = Paths.get(baseDir, oldFilePathStr);

            } else {
                dirFlag = oldFilePathStr.endsWith(File.separator);
                oldPath = Paths.get(oldFilePathStr);
                pathToMv = Paths.get(savePath, new File(oldFilePathStr).getName());
            }

            try {
                //위에서 saveStructureAsIs가 true 인 경우 필요한 디렉터리들을 만들었다면 이 부분은 빈 파일을 생성하는 과정이다.
                if (!Files.exists(pathToMv)) {
                    Files.createFile(pathToMv);
                }

                if (!dirFlag) {
                    if (Files.exists(oldPath)) {
                        Path moved = Files.move(oldPath, pathToMv, StandardCopyOption.REPLACE_EXISTING);
                        movedFileList.add(moved.toString());
                        ++successCount;
                    } else {
                        ++notExistInSource;
                    }
                } else {
                    pathsToMovedLast.put(oldPath, pathToMv);
                    ++dirCount;
                }
                if (debuggingWhenMoved) {
                    log.debug("[{}]File move success. Old path: \"{}\", Moved path: \"{}\"", txId, oldPath, pathToMv);
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

        if (!pathsToMovedLast.isEmpty()) {

            //DirectoryNotEmptyException을 방지하기 위해 최하위 파일부터 이동할 수 있도록 정렬한다.
            List<Path> sortingList = new ArrayList<>(pathsToMovedLast.keySet());
            Collections.sort(sortingList, new Comparator<Path>() {
                @Override
                public int compare(Path o1, Path o2) {
                    return (o2.toString()).compareToIgnoreCase(o1.toString());
                }
            });

            for (Path oldPath : sortingList) {
                Path pathToMv = pathsToMovedLast.get(oldPath);
                try {
                    try {
                        if (!Files.exists(pathToMv)) {
                            Files.move(oldPath, pathToMv, StandardCopyOption.REPLACE_EXISTING);
                        }
                        if ((oldPath.toFile()).list().length == 0) {
                            Files.deleteIfExists(oldPath);
                        }
                        ++successCount;
                        if (debuggingWhenMoved) {
                            log.debug("[{}]Directory move success. Old path: \"{}\", Moved path: \"{}\"", txId, oldPath, pathToMv);
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
        log.info("[{}]File movement result: inputFileList[file={} / directory={}], move_success={}, not_exist_in_source={}, error_count={}"
                , txId, inputListSize - dirCount, dirCount, successCount, notExistInSource, errorFilePaths.size());

    }

}
