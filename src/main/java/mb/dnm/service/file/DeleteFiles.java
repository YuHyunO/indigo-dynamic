package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileList;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;

import java.io.Serializable;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 파일 또는 디렉터리를 삭제한다.
 * <br>
 * <br>
 * *<b>Input</b>: 삭제할 파일의 경로<br>
 * *<b>Input type</b>: {@code String}, {@code List<String>}, {@code Set<String>}, {@code FileList}
 * <br>
 * <br>
 * *<b>Output</b>: 삭제된 파일의 삭제 전 경로<br>
 * *<b>Output type</b>: {@code List<String>}
 * <br>
 * <br>
 * *<b>Error Output</b>: 삭제를 실패한 파일 경로<br>
 * *<b>Error Output type</b>: {@code List<String>}
 * <br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.file.DeleteFiles"&gt;
 *     &lt;property name="input"                  value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                 value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 * &lt;/bean&gt;</pre>
 * @see mb.dnm.access.file.FileList
 * */
@Slf4j
@Setter
public class DeleteFiles extends SourceAccessService implements Serializable {

    private static final long serialVersionUID = 238893267637107314L;
    /**
     * 기본값: false
     * */
    private boolean ignoreErrorFile = false;
    private boolean debuggingWhenDeleted = true;


    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "DeleteFiles service must have the input parameter in which contain the files to delete");
        }

        InterfaceInfo info = ctx.getInfo();
        String txId = ctx.getTxId();

        //input으로 전달된 이동 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);
        List<String> targetFilePaths = new ArrayList<>();
        if (inputVal == null) {
            log.debug("[{}]The value of input '{}' is not found. No file paths to delete found in context data.", txId, getInput());
            return;
        }

        try {
            if (inputVal instanceof FileList) {
                FileList fileList = (FileList) inputVal;
                targetFilePaths = fileList.getFullFileList();

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

        List<String> deletedFileList = new ArrayList<>();
        List<String> errorFilePaths = new ArrayList<>();
        int inputListSize = targetFilePaths.size();
        int successCount = 0;
        int notExistInSource = 0;

        //DirectoryNotEmptyException을 방지하기 위해 최하위 파일부터 삭제할 수 있도록 정렬한다.
        List<String> sortingList = new ArrayList<>(targetFilePaths);
        Collections.sort(sortingList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareToIgnoreCase(o1);
            }
        });

        log.info("[{}]Deleting files ...", txId);
        for (String targetFilePath : sortingList) {
            Path path = Paths.get(targetFilePath);
            if (Files.exists(path)) {
                try {
                    Files.delete(path);
                    deletedFileList.add(targetFilePath);
                    ++successCount;
                    if (debuggingWhenDeleted) {
                        log.debug("[{}]The file '{}' is deleted.", txId, targetFilePath);
                    }
                } catch (Throwable t) {
                    if (ignoreErrorFile) {
                        errorFilePaths.add(targetFilePath);
                        log.warn("[{}]Exception occurred when deleting file but ignored. Cause: {}", txId, MessageUtil.toString(t));
                    } else {
                        throw t;
                    }
                }
            } else {
                ++notExistInSource;
                log.debug("[{}]The file '{}' is not exist.", txId, targetFilePath);
            }
        }

        if (getOutput() != null) {
            setOutputValue(ctx, deletedFileList);
        }
        if (getErrorOutput() != null) {
            if (!errorFilePaths.isEmpty()) {
                setErrorOutputValue(ctx, errorFilePaths);
            }
        }

        log.info("[{}]File delete result: input_count={}, delete_success={}, not_exist_in_source={}, error_count={}"
                , txId, inputListSize, successCount, notExistInSource, errorFilePaths.size());
    }
}
