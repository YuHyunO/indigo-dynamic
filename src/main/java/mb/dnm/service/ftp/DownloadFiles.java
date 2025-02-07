package mb.dnm.service.ftp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileList;
import mb.dnm.access.ftp.FTPSession;
import mb.dnm.code.DataType;
import mb.dnm.code.DirectoryType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.FileUtil;
import mb.dnm.util.MessageUtil;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * FTP 서버에서 파일을 다운로드 한다.
 * <br>
 * <br>
 * *<b>Input</b>: 다운로드 할 파일 또는 디렉터리의 경로<br>
 * *<b>Input type</b>: {@code String}, {@code List<String>}, {@code Set<String>}, {@code FileList}
 * <br>
 * <br>
 * *<b>Output</b>: 파일이 저장된 경로<br>
 * *<b>Output type</b>: {@code String} (1건), {@code List<String>} (N건)
 * <br>
 * *<b>Error Output</b>: 다운로드를 실패한 파일의 FTP 경로<br>
 * *<b>Error Output type</b>: {@code List<String>}
 * <br>
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.service.ftp.DownloadFiles"&gt;
 *     &lt;property name="sourceAlias"            value="<span style="color: black; background-color: #FAF3D4;">source alias</span>"/&gt;
 *     &lt;property name="directoryType"          value="<span style="color: black; background-color: #FAF3D4;">DirectoryType</span>"/&gt;
 *     &lt;property name="input"                  value="<span style="color: black; background-color: #FAF3D4;">input 파라미터명</span>"/&gt;
 *     &lt;property name="output"                 value="<span style="color: black; background-color: #FAF3D4;">output 파라미터명</span>"/&gt;
 * &lt;/bean&gt;</pre>
 *
 *
 * @see mb.dnm.service.ftp.ListFiles
 * @see mb.dnm.access.file.FileList
 * */
@Slf4j
@Setter
public class DownloadFiles extends AbstractFTPService implements Serializable {

    private static final long serialVersionUID = 7240209509936929547L;
    /**
     * outPutDataType 속성에 따라 파일의 데이터가 어떤 식으로 저장될 지 결정된다.<br>
     * 기본값: FILE<br>
     * -FILE (default) → {@code FileTemplate} 에서 directoryType과 일치하는 경로정보를 가져와서에서 파일로 저장된다.
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

    private DirectoryType directoryType = DirectoryType.LOCAL_RECEIVE;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "DownloadFiles service must have the input parameter in which contain the files to download");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = getFTPSourceName(info);
        String txId = ctx.getTxId();

        //input으로 전달된 다운로드 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);
        List<String> targetFileNames = new ArrayList<>();
        if (inputVal == null) {
            log.debug("[{}]The value of input '{}' is not found. No list of file path to download found in context data.", txId, getInput());
            return;
        }

        /*파일을 다운로드 할 때 FTP 서버의 디렉터리 구조 그대로 다운로드 할 지 판단하는 flag이다.
        input 데이터 타입이 FileList인 경우에만 적용된다.
        */
        boolean saveStructureAsIs = false;
        try {
            if (inputVal instanceof FileList) {
                FileList fileList = (FileList) inputVal;
                targetFileNames = fileList.getFileList();
                saveStructureAsIs = true;

            } else if (inputVal instanceof String) {
                targetFileNames.add((String) inputVal);

            } else if (inputVal instanceof List) {
                Set<String> tmpSet = new HashSet<>((List<String>) inputVal);
                if (tmpSet.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to download found in context data.", getInput());
                    return;
                }
                targetFileNames.addAll(tmpSet);
            } else if (inputVal instanceof Set) {
                Set<String> tmpSet = new HashSet<>((Set<String>) inputVal);
                if (tmpSet.isEmpty()) {
                    log.debug("The value of input '{}' is not found. No list of file path to download found in context data.", getInput());
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

        // 파일 다운로드 중 에러가 나는 경우 그 파일의 FTP 경로가 담길 리스트를 생성
        List<String> errorFilePaths = new ArrayList<>();
        int inputListSize = targetFileNames.size();
        int dirCount = 0;
        int successCount = 0;


        if (downloadType == DataType.FILE || downloadType == DataType.FILES) {
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

            Path path = Paths.get(savePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            List<String> localSavedPaths = new ArrayList<>();

            for (String ftpPath : targetFileNames) {
                Path localPath = null;
                
                //saveStructureAsIs가 true인 경우 즉, input 객체로 FileList가 전달된 경우 FTP 서버의 디렉터리 구조를 그대로 하여 파일을 다운로드 하기 위해 로컬에도 동일한 디렉터리 구조를 만드는 과정이다.
                if (saveStructureAsIs) {
                    StringBuffer dirToMadeBf = new StringBuffer();
                    dirToMadeBf.append(savePath)
                            .append(savePath.endsWith(File.separator) ? "" : File.separator);
                    dirToMadeBf.append(FileUtil.replaceToOSFileSeparator(ftpPath));

                    Path dirToMade = Paths.get(dirToMadeBf.toString());
                    if (dirToMadeBf.charAt(dirToMadeBf.length() - 1) != File.separatorChar) {
                        dirToMade = dirToMade.getParent();
                    }
                    if (!Files.exists(dirToMade)) {
                        Files.createDirectories(dirToMade);
                    }
                    localPath = dirToMade.resolve(new File(ftpPath).getName());
                } else {
                    localPath = Paths.get(savePath, new File(ftpPath).getName());
                }

                OutputStream os = null;
                try {
                    //위에서 saveStructureAsIs가 true 인 경우 필요한 디렉터리들을 만들었다면 이 부분은 빈 파일을 생성하는 과정이다.
                    if (!Files.exists(localPath)) {
                        Files.createFile(localPath);
                    }

                    os = Files.newOutputStream(localPath);

                    //파일경로가 디렉터리 구조인 경우에는 다운로드 시도 안함.
                    if (!(ftpPath.endsWith(pathSeparator))) {
                        if (ftp.retrieveFile(ftpPath, os)) {
                            localSavedPaths.add(localPath.toString());
                            ++successCount;
                            log.debug("[{}]FTP download success. FTP file: \"{}\", Local file: \"{}\"", txId, ftpPath, localPath);
                        } else {
                            Files.deleteIfExists(localPath);
                            log.warn("[{}]FTP download failed. File: \"{}\", Reply: {} ", txId, ftpPath, ftp.getReplyString().trim());
                            if (!ignoreErrorFile) {
                                throw new IllegalStateException("The file '" + ftpPath + "' is not found in the FTP server '" + srcName + "'.");
                            }
                        }
                    } else {
                        ++dirCount;
                    }

                } catch (Throwable t) {
                    if (getErrorOutput() != null && ignoreErrorFile) {
                        // 2. 다운로드에 실패한 파일은 건너뛰고 계속 다운로드를 진행한 후, 실패한 파일의 FTP 서버 경로를 error output 으로 output 한다.<br>
                        errorFilePaths.add(ftpPath);
                        log.warn("[{}]Exception occurred during download the file \"{}\", but continue the download. Cause: {}", txId, ftpPath, MessageUtil.toString(t));

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
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException ie) {}
                    }
                }
            }

            if (getOutput() != null) {
                if (downloadType == DataType.FILE) {
                    if (localSavedPaths.size() == 1) {
                        setOutputValue(ctx, localSavedPaths.get(0));
                    } else {
                        setOutputValue(ctx, localSavedPaths);
                    }
                } else if (downloadType == DataType.FILES) {
                    setOutputValue(ctx, localSavedPaths);
                }
            }
            if (getErrorOutput() != null) {
                if (!errorFilePaths.isEmpty()) {
                    if (downloadType == DataType.FILE) {
                        if (errorFilePaths.size() == 1) {
                            setErrorOutputValue(ctx, errorFilePaths.get(0));
                        } else {
                            setErrorOutputValue(ctx, errorFilePaths);
                        }
                    } else if (downloadType == DataType.FILES) {
                        setErrorOutputValue(ctx, errorFilePaths);
                    }
                }
            }

        } else if (downloadType == DataType.BYTE_ARRAY) {
            // downloadType 이 dataTypeDataType.BYTE_ARRAY 인 경우 List<Map<String, byte[]>> 즉, List<Map<파일명, byte array형태의 파일데이터>> 형태로 ContextData에 저장됨
            List<Map<String, byte[]>> resultFileData = new ArrayList<>();

            for (String ftpPath : targetFileNames) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    //파일경로가 디렉터리 구조인 경우에는 다운로드 시도 안함.
                    if (!(ftpPath.endsWith("/") || ftpPath.endsWith("\\"))) {
                        if (ftp.retrieveFile(ftpPath, os)) {
                            Map<String, byte[]> data = new HashMap<>();
                            data.put(new File(ftpPath).getName(), os.toByteArray());
                            resultFileData.add(data);
                            ++successCount;
                            log.debug("[{}]FTP download success. FTP file: \"{}\" saved as a byte array", txId, ftpPath);
                        } else {
                            log.warn("[{}]FTP download failed. File: \"{}\", Reply: {} ", txId, ftpPath, ftp.getReplyString().trim());
                        }
                    } else {
                        ++dirCount;
                    }
                } catch (Throwable t) {
                    // 파일 다운로드 중 에러 발생하는 경우 무시하여 진행하는 옵션
                    if (getErrorOutput() != null && ignoreErrorFile) {
                        errorFilePaths.add(ftpPath);
                        log.warn("[{}]Exception occurred during download the file \"{}\", but continue the download. Cause: {}", txId, ftpPath, MessageUtil.toString(t));
                    } else {
                        log.warn("[{}]FTP file download failed.", txId);
                        throw t;
                    }
                } finally {
                    os.close();
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

        log.info("[{}]FTP file download result: inputFileList[file={} / directory={}], download_success={}, error_count={}, file_type={}"
                , txId, inputListSize - dirCount, dirCount, successCount, errorFilePaths.size(), downloadType);

    }




    public void setDownloadType(DataType downloadType) {
        switch (downloadType) {
            case FILE: case BYTE_ARRAY: break;
            default: throw new IllegalArgumentException("Not supported data type: " + downloadType);
        }
        this.downloadType = downloadType;
    }

}
