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

import java.io.Serializable;
import java.util.*;

/**
 * FTP서버의 파일 또는 디렉터리를 삭제한다.
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
 * &lt;bean class="mb.dnm.service.ftp.DeleteFiles"&gt;
 *     &lt;property name="sourceAlias"            value="<span style="color: black; background-color: #FAF3D4;">source alias</span>"/&gt;
 *     &lt;property name="input"                  value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                 value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @see mb.dnm.access.file.FileList
 *
 * */
@Slf4j
@Setter
public class DeleteFiles extends AbstractFTPService implements Serializable {
    private static final long serialVersionUID = 694107796554156688L;
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
        if (session == null) {
            new FTPLogin(getSourceAlias()).process(ctx);
            session = (FTPSession) ctx.getSession(srcName);
        }
        if (!session.isConnected()) {
            new FTPLogin(getSourceAlias()).process(ctx);
            session = (FTPSession) ctx.getSession(srcName);
        }
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
