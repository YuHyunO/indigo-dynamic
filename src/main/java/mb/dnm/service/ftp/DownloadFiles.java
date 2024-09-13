package mb.dnm.service.ftp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.code.DataType;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.FileTemplate;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Input으로 전달받은 파일 경로로부터 파일의 데이터를 다운로드한다.<br>
 * dataType 속성이 FILE인 경우, 지정된 경로로 파일이 저장되며 다운로드 받은 File이 저장된 경로가 리스트로 output 된다.(지정된 저장 경로가 존재하지 않는 경우 새로 생성함)<br> dataType 속성이 BYTE_ARRAY 인 경우,
 * 파일명과 파일의 데이터가 <code>Map&lt;String, byte[]&gt;</code> 형태로 담긴 리스트가 output 된다.<br>
 * FTP 파일의 데이터가 파일로 저장되도록 설정된 경우, 어느 경로에 파일을 저장할 지에 대한 정보는 <code>InterfaceInfo</code> 에 저장된 <code>FileTemplate</code> 의 속성들로부터 가져온다.<br>
 * <p><i>
 * 파일을 다운로드 하는 도중 에러가 발생하는 경우에 다음과 같이 처리하도록 설정할 수 있다.<br><br>
 *
 * 1. 파일 다운로드 중 다운로드에 실패하면 다운로드에 성공했던 모든 파일을 파일 저장경로에서 삭제한 뒤 <code>Exception</code> 을 throw 한다.(기본 설정값)
 * (<code>downloadType</code> 이 BYTE_ARRAY인 경우 삭제절차는 없음)<br>
 * →설정방법: <code>deleteDownloadedFileWhenError</code>을 <code>true</code>로 지정하고 <code>ignoreErrorFile</code>을 <code>false</code>로 설정한다.<br><br>
 *
 * 2. 다운로드에 실패한 파일은 건너뛰고 계속 다운로드를 진행한 후, 실패한 파일의 FTP 서버 경로를 error output 으로 output 한다.<br>
 * →설정방법: <code>errorOutput</code>을 지정하고, <code>ignoreErrorFile</code>을 <code>true</code>로 설정한다.<br><br>
 *
 * 3. 파일 다운로드 중 다운로드에 실패하면 다운로드에 성공한 파일은 그대로 두고 <code>Exception</code> 을 throw 한다.
 * (단, <code>downloadType</code> 이 BYTE_ARRAY인 경우에는 byte array 데이터가 보존되지 않음)<br>
 * →설정방법: <code>deleteDownloadedFileWhenError</code>을 <code>false</code>로 지정하고 <code>ignoreErrorFile</code>을 <code>false</code>로 설정한다.<br><br>
 * 
 *</i></p>
 *
 * 보통 <code>mb.dnm.service.ftp.ListFiles</code> 서비스와 연계해서 사용한다.
 *
 * @see mb.dnm.service.ftp.ListFiles
 *
 * @author Yuhyun O
 * @version 2024.09.12
 *
 * @Input 다운로드 받을 파일의 FTP서버 경로
 * @InputType <code>String</code>(1건) 또는 <code>List&lt;String&gt;</code>(여러건)
 * @Output downloadType 속성이 FILE인 경우, 지정된 경로로 파일이 저장되며 다운로드 받은 File이 저장된 경로가 리스트로 output 된다.<br> outPutDataType 속성이 BYTE_ARRAY 인 경우,
 * 파일명과 파일의 데이터가 <code>Map&lt;String, byte[]&gt;</code> 형태로 담긴 리스트가 output 된다.
 * @OutputType dataType(FILE): <code>List&lt;String&gt;</code>, dataType(BYTE_ARRAY): <code>List&lt;Map&lt;String, byte[]&gt;&gt;</code>
 * @ErrorOutput 파일을 다운로드 하는 중 에러가 발생하여 다운로드에 실패하는 경우 에러가 발생한 파일의 FTP서버 경로
 * @ErrorOutputType <code>List&lt;String&gt;</code>
 * */
@Slf4j
@Setter
public class DownloadFiles extends AbstractFTPService {

    /**
     * outPutDataType 속성에 따라 파일의 데이터가 어떤 식으로 저장될 지 결정된다.<br>
     * 기본값: FILE<br>
     * -FILE (default) → <code>FileTemplate</code> 에서 directoryType과 일치하는 경로정보를 가져와서에서 파일로 저장된다.
     * -BYTE_ARRAY → byte array 형태로 output 된다.
     * */
    private DataType downloadType = DataType.FILE;
    /**
     * 기본값: false
     * */
    private boolean ignoreErrorFile = false;
    /**
     * 기본값: true
     * */
    private boolean deleteDownloadedFileWhenError = true;



    /**
     * outPutDataType 속성이 DataType.FILE 인 경우에만 이 속성이 유효하다.<br>
     * directoryType 속성에 따라 <code>FileTemplate</code>에서 어떤 속성의 값을 목록을 가져올 경로로써 사용할 지 결정된다.<br>
     * 이 속성을 설정하는데 있어 주의할 점은 설정한 directoryType이 REMOTE이든 LOCAL이든 상관없이 파일은 현재 어댑터 어플리케이션이 작동중인 하드웨어의 지정된 경로에 저장이 된다.
     * 따라서 REMOTE라는 prefix를 가진 directoryType을 설정하더라도 원격 서버에 파일이 전송되어 저장되지 않는다.<br>
     * 
     * -기본값: <code>LOCAL_RECEIVE</code><br>
     * -REMOTE_SEND → <code>FileTemplate#remoteSendDir</code> 에 파일이 저장됨<br>
     * -REMOTE_RECEIVE → <code>FileTemplate#remoteReceiveDir</code> 에 파일이 저장됨<br>
     * -REMOTE_TEMP → <code>FileTemplate#remoteTempDir</code> 에 파일이 저장됨<br>
     * -REMOTE_SUCCESS → <code>FileTemplate#remoteSuccessDir</code> 에 파일이 저장됨<br>
     * -REMOTE_ERROR → <code>FileTemplate#remoteErrorDir</code> 에 파일이 저장됨<br>
     * -REMOTE_BACKUP → <code>FileTemplate#remoteBackupDir</code> 에 파일이 저장됨<br>
     * -LOCAL_SEND → <code>FileTemplate#localSendDir</code> 에 파일이 저장됨<br>
     * -LOCAL_RECEIVE → <code>FileTemplate#localReceiveDir</code> 에 파일이 저장됨<br>
     * -LOCAL_TEMP → <code>FileTemplate#localTempDir</code> 에 파일이 저장됨<br>
     * -LOCAL_SUCCESS → <code>FileTemplate#localSuccessDir</code> 에 파일이 저장됨<br>
     * -LOCAL_ERROR → <code>FileTemplate#localErrorDir</code> 에 파일이 저장됨<br>
     * -LOCAL_BACKUP → <code>FileTemplate#localBackupDir</code> 에 파일이 저장됨<br>
     * */
    private DirectoryType directoryType = DirectoryType.LOCAL_RECEIVE;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "DownloadFiles service must have the input parameter in which contain");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = getFTPSourceName(info);
        String txId = ctx.getTxId();

        //input으로 전달된 다운로드 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);
        List<String> targetFileNames = new ArrayList<>();
        if (inputVal == null) {
            log.debug("The value of input '{}' is not found. No list of file path to download found in context data.", getInput());
            return;
        }

        try {
            if (inputVal instanceof String) {
                targetFileNames.add((String) inputVal);
            } else if (inputVal instanceof List) {
                List<String> tmpList = (List<String>) inputVal;
                if (tmpList.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to download found in context data.", getInput());
                    return;
                }
                targetFileNames.addAll(tmpList);
            } else {
                throw new ClassCastException();
            }
        } catch (ClassCastException ce) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The type of the input parameter value is not String or List<String>. Inputted value's type: " + inputVal.getClass().getName());
        }


        FTPSession session = (FTPSession) ctx.getSession(srcName);
        if (session == null) {
            new FTPLogin().process(ctx);
        }
        FTPClient ftp = session.getFTPClient();

        // 파일 다운로드 중 에러가 나는 경우 그 파일의 FTP 경로를 저장할 리스트를 생성
        List<String> errorFilePaths = new ArrayList<>();
        int success = 0;

        if (downloadType == DataType.FILE) {
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

            Path path = Paths.get(savePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            List<String> localSavedPaths = new ArrayList<>();

            for (String ftpPath : targetFileNames) {
                Path localPath = Paths.get(savePath, new File(ftpPath).getName());
                BufferedOutputStream bos = null;
                try {
                    if (!Files.exists(localPath)) {
                        Files.createFile(localPath);
                    }

                    bos = new BufferedOutputStream(Files.newOutputStream(localPath));
                    if (ftp.retrieveFile(ftpPath, bos)) {
                        localSavedPaths.add(localPath.toString());
                        ++success;
                        log.debug("[{}]FTP file is downloaded. The file '{}' saved as a locale file '{}'", txId, ftpPath, localPath);
                    } else {
                        Files.deleteIfExists(localPath);
                        log.debug("[{}]FTP file is not downloaded. The file name is '{}'", txId, ftpPath);
                    }
                } catch (Throwable t) {
                    if (getErrorOutput() != null && ignoreErrorFile) {
                        // 2. 다운로드에 실패한 파일은 건너뛰고 계속 다운로드를 진행한 후, 실패한 파일의 FTP 서버 경로를 error output 으로 output 한다.<br>
                        errorFilePaths.add(ftpPath);
                        log.warn("[{}]Exception occurred during download the file '{}', but continue the download. Cause: {}", txId, ftpPath, MessageUtil.toString(t));

                    } else if (!deleteDownloadedFileWhenError && !ignoreErrorFile) {
                        //3. 파일 다운로드 중 다운로드에 실패하면 다운로드에 성공한 파일은 그대로 두고 Exception 을 throw 한다.
                        throw t;

                    } else {
                        //1. 파일 다운로드 중 다운로드에 실패하면 다운로드에 성공했던 모든 파일을 파일 저장경로에서 삭제한 뒤 Exception 을 throw 한다.(기본 설정값)
                        //그 외
                        for (String succFile : localSavedPaths) {
                            Path succPath = Paths.get(succFile);
                            if (Files.exists(succPath)) {
                                try {
                                    Files.delete(succPath);
                                } catch (IOException e) {
                                    log.warn("[{}]Failed to delete success file when error. Cause", MessageUtil.toString(t));
                                }
                            }
                        }
                        throw t;
                    }
                } finally {
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException ie) {}
                    }
                }
            }

            if (getOutput() != null) {
                setOutputValue(ctx, localSavedPaths);
            }
            if (getErrorOutput() != null) {
                if (!errorFilePaths.isEmpty()) {
                    setErrorOutputValue(ctx, errorFilePaths);
                }
            }

        } else if (downloadType == DataType.BYTE_ARRAY) {
            // downloadType 이 dataTypeDataType.BYTE_ARRAY 인 경우 List<Map<String, byte[]>> 즉, List<Map<파일명, byte array형태의 파일데이터>> 형태로 ContextData에 저장됨
            List<Map<String, byte[]>> resultFileData = new ArrayList<>();

            for (String ftpPath : targetFileNames) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    if (ftp.retrieveFile(ftpPath, bos)) {
                        Map<String, byte[]> data = new HashMap<>();
                        data.put(new File(ftpPath).getName(), bos.toByteArray());
                        resultFileData.add(data);
                        ++success;
                        log.debug("[{}]FTP file is downloaded. The file '{}' saved as a byte array", txId, ftpPath);
                    } else {
                        log.debug("[{}]FTP file is not downloaded. The file name is '{}'", txId, ftpPath);
                    }
                } catch (Throwable t) {
                    // 파일 다운로드 중 에러 발생하는 경우 무시하여 진행하는 옵션
                    if (getErrorOutput() != null && ignoreErrorFile) {
                        errorFilePaths.add(ftpPath);
                        log.warn("[{}]Exception occurred during download the file '{}', but continue the download. Cause: {}", txId, ftpPath, MessageUtil.toString(t));
                    } else {
                        log.warn("[{}]FTP file download failed.", txId);
                        throw t;
                    }
                } finally {
                    bos.close();
                }
            }
            if (getOutput() != null) {
                setOutputValue(ctx, resultFileData);
            }
            if (getErrorOutput() != null) {
                if (!errorFilePaths.isEmpty()) {
                    setErrorOutputValue(ctx, errorFilePaths);
                }
            }
        }

        log.info("[{}]FTP file download result(inputted count: {}, downloaded count: {}, download type: {}, error count: {}) "
                , txId, targetFileNames.size(), success, downloadType, errorFilePaths.size());

    }



    public void setDownloadType(DataType downloadType) {
        switch (downloadType) {
            case FILE: case BYTE_ARRAY: break;
            default: throw new IllegalArgumentException("Not supported data type: " + downloadType);
        }
        this.downloadType = downloadType;
    }

}
