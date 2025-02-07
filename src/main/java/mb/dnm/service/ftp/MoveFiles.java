package mb.dnm.service.ftp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileList;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.code.DirectoryType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.FTPUtil;
import mb.dnm.util.FileUtil;
import mb.dnm.util.MessageUtil;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 동일한 FTP 서버 내의 파일을 이동한다.
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
 * &lt;bean class="mb.dnm.service.ftp.MoveFiles"&gt;
 *     &lt;property name="sourceAlias"            value="<span style="color: black; background-color: #FAF3D4;">source alias</span>"/&gt;
 *     &lt;property name="directoryType"          value="<span style="color: black; background-color: #FAF3D4;">DirectoryType</span>"/&gt;
 *     &lt;property name="input"                  value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                 value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @see mb.dnm.service.ftp.ListFiles
 * @see mb.dnm.access.file.FileList
 */
@Slf4j
@Setter
public class MoveFiles extends AbstractFTPService implements Serializable {
    private static final long serialVersionUID = 1792374178458720119L;

    private DirectoryType directoryType = DirectoryType.REMOTE_MOVE;
    /**
     * 기본값: false
     * */
    private boolean ignoreErrorFile = false;
    private boolean debuggingWhenMoved = true;
    /**
     * 기본값: true<br>
     * 이동하려는 FTP 서버 경로에 파일이 이미 존재하는 경우 덮어쓰기를 한다.
     * */
    private boolean overwrite = true;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "MoveFiles service must have the input parameter in which contain the files to move");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = getFTPSourceName(info);
        String txId = ctx.getTxId();

        Object inputVal = getInputValue(ctx);
        List<String> targetFileNames = new ArrayList<>();
        if (inputVal == null) {
            log.debug("[{}]The value of input '{}' is not found. No list of file path to move found in context data.", txId, getInput());
            return;
        }

        String baseDir = null;
        try {
            if (inputVal instanceof FileList) {
                FileList fileList = (FileList) inputVal;
                targetFileNames = fileList.getFileList();
                baseDir = fileList.getBaseDirectory();

            } else if (inputVal instanceof String) {
                targetFileNames.add((String) inputVal);

            } else if (inputVal instanceof List) {
                Set<String> tmpSet = new HashSet<>((List<String>) inputVal);
                if (tmpSet.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to move found in context data.", getInput());
                    return;
                }
                targetFileNames.addAll(tmpSet);
            } else if (inputVal instanceof Set) {
                Set<String> tmpSet = new HashSet<>((Set<String>) inputVal);
                if (tmpSet.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to move found in context data.", getInput());
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
        String pathSeparator = String.valueOf(ftp.printWorkingDirectory().charAt(0)).trim();
        if (pathSeparator.equals("null") || pathSeparator.isEmpty()) {
            pathSeparator = "/";
        }

        List<String> movedFileList = new ArrayList<>();
        // 파일 이동 중 에러가 나는 경우 그 파일의 FTP 경로가 담길 리스트를 생성
        List<String> errorFilePaths = new ArrayList<>();
        int inputListSize = targetFileNames.size();
        int successCount = 0;

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
        //PlaceHolder mapping 을 적용할 것
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

        if (!savePath.endsWith(pathSeparator)) {
            savePath = savePath + pathSeparator;
        }

        String[] savePaths = savePath.split(pathSeparator);
        StringBuilder pathBd = new StringBuilder();
        for (int i = 0; i < savePaths.length; i++) {
            String path = pathBd.append(pathSeparator).append(savePaths[i]).toString();
            if (!FTPUtil.isDirectoryExists(ftp, path)) {
                ftp.makeDirectory(path);
            }
        }

        Collections.sort(targetFileNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareToIgnoreCase(o1);
            }
        });

        for (String targetFile : targetFileNames) {
            String tmpTargetFile = null;
            String oldPath = null;
            String newPath = null;
            if (baseDir != null) {
                oldPath = baseDir + targetFile;
                newPath = savePath + targetFile;
            } else {

                if (targetFile.endsWith(pathSeparator)) {
                    tmpTargetFile = targetFile.substring(0, targetFile.length() - 1);
                    tmpTargetFile = tmpTargetFile.substring(tmpTargetFile.lastIndexOf(pathSeparator) + 1);
                } else {
                    int lastPathSepIdx = targetFile.lastIndexOf(pathSeparator);
                    if (lastPathSepIdx != -1) {
                        tmpTargetFile = targetFile.substring(lastPathSepIdx + 1);
                    } else {
                        tmpTargetFile = targetFile;
                    }
                }
                oldPath = targetFile;
                newPath = savePath + tmpTargetFile;
            }

            String originalNewPath = newPath;
            boolean overwritten = false;
            boolean moved = false;
            try {
                int i = 0;
                while (true) {
                    //파일 덮어쓰기 옵션이 true인 경우 파일을 이동할 때 복사본을 먼저 만든다.
                    if (FTPUtil.isFileExists(ftp, newPath)) {
                        if (overwrite) {
                            //ftp.deleteFile(newPath);
                            //newPath =  newPath + "_" + (++i);
                            newPath =  savePath + "$" + tmpTargetFile + "(" + (++i) + ")";
                            overwritten = true;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                moved = ftp.rename(oldPath, newPath);
                if (moved) {
                    if (overwritten) {
                        log.info("[{}]Overwriting file '{}'...", txId, originalNewPath);
                        ftp.deleteFile(originalNewPath);
                        ftp.rename(newPath, originalNewPath);
                    }
                    ++successCount;
                    movedFileList.add(newPath);
                    if (debuggingWhenMoved) {
                        log.debug("[{}]The file is moved from '{}' to '{}'", txId, oldPath, originalNewPath);
                    }
                } else {
                    String reply = ftp.getReplyString();
                    log.debug("[{}]Could not move the file '{}' to '{}'. Reply: {}", txId, oldPath, newPath, reply);
                    throw new IllegalStateException("Could not move the file '" + oldPath + "' to '" + newPath + "'. Reply: " + reply);
                }
            } catch (Throwable t) {
                if (ignoreErrorFile) {
                    // 이동에 실패한 파일은 건너뛰고 계속 이동을 진행한 후, 실패한 파일의 FTP 서버 경로를 error output 으로 output 한다.<br>
                    errorFilePaths.add(oldPath);
                    log.warn("[{}]Exception occurred during move the file \"{}\", but continue to move. Cause: {}", txId, oldPath, MessageUtil.toString(t));
                } else {
                    throw t;
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
        log.info("[{}]File movement result: input_count={}, move_success={}, error_count={}"
                , txId, inputListSize, successCount, errorFilePaths.size());

    }

}
