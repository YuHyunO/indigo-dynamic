package mb.dnm.service.ftp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileList;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;
import org.apache.commons.net.ftp.FTPClient;

import java.util.*;

/**
 * FTP서버의 파일 또는 디렉터리를 삭제한다.
 *
 * @see mb.dnm.access.file.FileList
 *
 * @author Yuhyun O
 * @version 2024.09.17
 *
 * @Input 삭제할 파일의 경로
 * @InputType <code>String</code>(1건) 또는 <code>List&lt;String&gt;</code> 또는 <code>Set&lt;String&gt;</code> 또는 <code>FileList</code><br>
 * input이 List로 전달되는 경우 중복된 경로가 존재하더라도 내부적으로 Set 객체에 다시 담기게 되므로 중복값이 제거된다.
 * @Output 삭제된 파일의 삭제 전 경로
 * @OutputType <code>List&lt;String&gt;</code>
 * @ErrorOutput 파일을 삭제하는 중 에러가 발생하여 삭제에 실패하는 경우 에러가 발생한 파일의 경로
 * @ErrorOutputType <code>List&lt;String&gt;</code>
 * */
@Slf4j
@Setter
public class DeleteFiles extends AbstractFTPService {
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
        String srcName = getFTPSourceName(info);
        String txId = ctx.getTxId();

        //input으로 전달된 다운로드 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);
        List<String> targetFileNames = new ArrayList<>();
        if (inputVal == null) {
            log.debug("[{}]The value of input '{}' is not found. No list of file path to delete found in context data.", txId, getInput());
            return;
        }
        
        try {
            if (inputVal instanceof FileList) {
                FileList fileList = (FileList) inputVal;
                targetFileNames = fileList.getFullFileList();

            } else if (inputVal instanceof String) {
                targetFileNames.add((String) inputVal);

            } else if (inputVal instanceof List) {
                Set<String> tmpSet = new HashSet<>((List<String>) inputVal);
                if (tmpSet.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to delete found in context data.", getInput());
                    return;
                }
                targetFileNames.addAll(tmpSet);
            } else if (inputVal instanceof Set) {
                Set<String> tmpSet = new HashSet<>((Set<String>) inputVal);
                if (tmpSet.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to delete found in context data.", getInput());
                    return;
                }
                targetFileNames.addAll(tmpSet);
            } else {
                throw new ClassCastException();
            }
        } catch (ClassCastException ce) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The type of the input parameter value is not String or List<String> or Set<String> or FileList. Inputted value's type: " + inputVal.getClass().getName());
        }

        FTPSession session = getFTPSession(ctx, srcName);
        FTPClient ftp = session.getFTPClient();

        // 파일 삭제 중 에러가 나는 경우 그 파일의 FTP 경로가 담길 리스트를 생성
        List<String> errorFilePaths = new ArrayList<>();
        int inputListSize = targetFileNames.size();
        int successCount = 0;

        // 최하위 파일부터 삭제하기 위한 정렬
        Collections.sort(targetFileNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareToIgnoreCase(o1);
            }
        });

        List<String> deletedFileList = new ArrayList<>();
        log.info("[{}]Deleting files ...", txId);
        for (String targetFileName : targetFileNames) {
            try {
                boolean deleted = false;
                if (targetFileName.endsWith("/")) {
                    deleted = ftp.removeDirectory(targetFileName);
                } else {
                    deleted = ftp.deleteFile(targetFileName);
                }
                if (deleted) {
                    ++successCount;
                    deletedFileList.add(targetFileName);
                    if (debuggingWhenDeleted) {
                        log.debug("[{}]The file '{}' is deleted.", txId, targetFileName);
                    }
                } else {
                    String reply = ftp.getReplyString();
                    log.debug("[{}]Could not delete the file '{}'. Reply: {}", txId, targetFileName, reply);
                }
            } catch (Throwable t) {
                if (ignoreErrorFile) {
                    errorFilePaths.add(targetFileName);
                    log.warn("[{}]Exception occurred when deleting file but ignored. Cause: {}", txId, MessageUtil.toString(t));
                } else {
                    throw t;
                }
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
        log.info("[{}]File delete result: input_count={}, delete_success={}, error_count={}"
                , txId, inputListSize, successCount, errorFilePaths.size());
    }
}
