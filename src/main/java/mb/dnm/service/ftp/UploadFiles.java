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
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * FTP 서버에 파일을 업로드 한다.
 * <br>
 * <br>
 * *<b>Input</b>: 업로드 할 파일의 로컬 경로<br>
 * *<b>Input type</b>: {@code String}, {@code List<String>}, {@code Set<String>}, {@code FileList}
 * <br>
 * <br>
 * *<b>Output</b>: 파일이 업로드된 FTP 서버 경로<br>
 * *<b>Output type</b>: {@code List<String>}
 * <br>
 * *<b>Error Output</b>: 업로드를 실패한 파일의 FTP 경로<br>
 * *<b>Error Output type</b>: {@code List<String>}
 * <br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.ftp.UploadFiles"&gt;
 *     &lt;property name="sourceAlias"            value="<span style="color: black; background-color: #FAF3D4;">source alias</span>"/&gt;
 *     &lt;property name="directoryType"          value="<span style="color: black; background-color: #FAF3D4;">DirectoryType</span>"/&gt;
 *     &lt;property name="input"                  value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                 value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @see mb.dnm.service.ftp.ListFiles
 * @see mb.dnm.access.file.FileList
 * */
@Slf4j
@Setter
public class UploadFiles extends AbstractFTPService implements Serializable {
    private static final long serialVersionUID = -1038608024349166103L;

    private DirectoryType directoryType = DirectoryType.LOCAL_SEND;
    /**
     * 기본값: false
     * */
    private boolean ignoreErrorFile = false;
    private boolean debuggingWhenUploaded = true;
    /**
     * 기본값: true<br>
     * 업로드하려는 파일이 이미 존재하는 경우 덮어쓰기를 한다.
     * */
    private boolean overwrite = true;
    /**
     * 기본값: true
     * 파일 업로드 중 에러가 발생하는 경우 업로드한 파일을 모두 삭제한다. ignoreErrorFile 속성이 false인 경우에만 유효하다.
     * */
    private boolean deleteUploadedFileWhenError = false;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "UploadFiles service must have the input parameter in which contain the files to upload");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = getFTPSourceName(info);
        String txId = ctx.getTxId();

        Object inputVal = getInputValue(ctx);
        List<String> targetFileNames = new ArrayList<>();
        if (inputVal == null) {
            log.debug("[{}]The value of input '{}' is not found. No list of file path to upload found in context data.", txId, getInput());
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
                    log.debug("The value of input '{}' is not found. No list of file path to upload found in context data.", getInput());
                    return;
                }
                targetFileNames.addAll(tmpSet);
            } else if (inputVal instanceof Set) {
                Set<String> tmpSet = new HashSet<>((Set<String>) inputVal);
                if (tmpSet.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to upload found in context data.", getInput());
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
        String ftpPathSeparator = String.valueOf(ftp.printWorkingDirectory().charAt(0)).trim();
        if (ftpPathSeparator.equals("null") || ftpPathSeparator.isEmpty()) {
            ftpPathSeparator = "/";
        }

        List<String> uploadedFileList = new ArrayList<>();
        // 파일 업로드 중 에러가 나는 경우 그 파일의 FTP 경로가 담길 리스트를 생성
        List<String> errorFilePaths = new ArrayList<>();
        int inputListSize = targetFileNames.size();
        int successCount = 0;
        int dirCount = 0;
        int fileCount = 0;

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

        if (!savePath.endsWith(ftpPathSeparator)) {
            savePath = savePath + ftpPathSeparator;
        }

        if (!FTPUtil.isDirectoryExists(ftp, savePath)) {
            StringBuilder pathBd = null;
            if (savePath.indexOf(ftpPathSeparator) == 0) {
                pathBd = new StringBuilder(ftpPathSeparator);
            } else {
                pathBd = new StringBuilder();
            }
            for (String dirToMade : savePath.split(ftpPathSeparator)) {
                dirToMade = dirToMade.trim();
                pathBd.append(dirToMade);
                if (dirToMade.equals("..") || dirToMade.equals(".")) {
                    continue;
                }
                ftp.makeDirectory(pathBd.toString());
                pathBd.append(ftpPathSeparator);
            }

            log.info("[{}]The directory \"{}\" is made at FTP server",  txId, savePath);
        }

        Collections.sort(targetFileNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });

        /*
        * 파일을 본래의 구조대로(디렉터리 구조) 업로드 하려면 FileList 객체를 사용해야만 함
        * */
        for (String targetFileName : targetFileNames) {
            String tmpTargetFileName = FileUtil.replaceSeparator(targetFileName, ftpPathSeparator);
            String localPath = null;
            String remotePath = null;

            if (baseDir != null) {
                localPath = baseDir + targetFileName;
                remotePath = savePath + tmpTargetFileName;
            } else {
                localPath = targetFileName;
                tmpTargetFileName = new File(tmpTargetFileName).getName();
                remotePath = savePath + tmpTargetFileName;
            }
            if (!Files.exists(Paths.get(localPath))) {
                log.warn("[{}]There is no file with name '{}'", txId, localPath);
                continue;
            }

            InputStream is = null;
            String existRemotePath = remotePath;
            boolean overwritten = false;
            boolean existFileDeleted = false;
            try {
                Path path = Paths.get(localPath);
                if (Files.isDirectory(path)) {
                    if (ftp.makeDirectory(remotePath)) {
                        uploadedFileList.add(remotePath);
                        ++dirCount;
                        ++successCount;
                    }
                    continue;
                }
                int i = 0;
                while (true) {
                    //파일 덮어쓰기 옵션이 true인 경우 파일을 이동할 때 복사본을 먼저 만든다.
                    if (FTPUtil.isFileExists(ftp, remotePath)) {
                        if (overwrite) {
                            //remotePath = remotePath + "(" + (++i) + ")";
                            remotePath = savePath + "$" + tmpTargetFileName + "(" + (++i) + ")";
                            overwritten = true;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                is = Files.newInputStream(path);
                boolean uploaded = ftp.storeFile(remotePath, is);
                if (uploaded) {
                    if (overwritten) {
                        log.info("[{}]Overwriting file '{}'...", txId, existRemotePath);
                        ftp.deleteFile(existRemotePath);
                        ftp.rename(remotePath, existRemotePath);
                    }

                    uploadedFileList.add(remotePath);
                    ++fileCount;
                    ++successCount;
                    if (debuggingWhenUploaded) {
                        log.debug("[{}]The file \"{}\" is upload to \"{}\"", txId, localPath, existRemotePath);
                    }
                } else {
                    String reply = ftp.getReplyString();
                    if (ignoreErrorFile) {
                        errorFilePaths.add(localPath.toString());
                        log.warn("[{}]Couldn't upload the file \"{}\" to \"{}\", but continue to upload. Reply: {}", txId, localPath, remotePath, reply);
                    } else {
                        throw new IllegalStateException("Couldn't upload the file \"" + localPath + "\" to \"" + remotePath + "\". FTP reply: " + reply);
                    }
                }

            } catch (Throwable t) {
                if (ignoreErrorFile) {
                    errorFilePaths.add(localPath.toString());
                    log.warn("[{}]Exception occurred during upload the file \"{}\", but continue to upload. Cause: {}", txId, localPath, MessageUtil.toString(t));
                } else {
                    if (deleteUploadedFileWhenError) {
                        Collections.sort(targetFileNames, new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return o2.compareToIgnoreCase(o1);
                            }
                        });
                        for (String fileToDelete : uploadedFileList) {
                            if (fileToDelete.endsWith(ftpPathSeparator)) {
                                ftp.removeDirectory(targetFileName);
                            } else {
                                ftp.deleteFile(targetFileName);
                            }
                        }
                    }
                    throw t;
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        if (getOutput() != null) {
            setOutputValue(ctx, uploadedFileList);
        }
        if (getErrorOutput() != null) {
            if (!errorFilePaths.isEmpty()) {
                setErrorOutputValue(ctx, errorFilePaths);
            }
        }
        log.info("[{}]FTP file upload result: input_count={}, uploaded_file={}, uploaded_directory={}, upload_success={}, error_count={}"
                , txId, inputListSize, fileCount, dirCount, successCount, errorFilePaths.size());
    }


}
