package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileList;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 파일 또는 디렉터리를 삭제한다.
 *
 * @see mb.dnm.access.file.FileList
 *
 * @author Yuhyun O
 * @version 2024.09.17
 *
 * @Input 삭제할 파일의 경로
 * @InputType <code>String</code>(1건) 또는 <code>List&lt;String&gt;</code> 또는 <code>Set&lt;String&gt;</code> 또는 <code>FileList</code><br>
 * input이 List로 전달되는 경우 중복된 경로가 존재하더라도 내부적으로 Set 객체에 다시 담기게 되므로 중복값이 제거된다.
 * @Output 삭제할 파일의 삭제 후 경로 리스트
 * @OutputType <code>List&lt;String&gt;</code>
 * @ErrorOutput 파일을 삭제하는 중 에러가 발생하여 삭제에 실패하는 경우 에러가 발생한 파일의 경로
 * @ErrorOutputType <code>List&lt;String&gt;</code>
 * */
@Slf4j
@Setter
public class DeleteFiles extends SourceAccessService {

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
            log.debug("The value of input '{}' is not found. No file paths to move found in context data.", getInput());
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

        List<String> movedFileList = new ArrayList<>();
        List<String> errorFilePaths = new ArrayList<>();
        int inputListSize = targetFilePaths.size();
        int successCount = 0;
        int notExistInSource = 0;

        //최상위 파일부터 삭제할 수 있도록 정렬한다.
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
            setOutputValue(ctx, movedFileList);
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
