package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileParser;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.code.DirectoryType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;
import mb.dnm.util.SortingUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * 지정된 경로에 파일을 생성한다.
 * <br>
 * 파일의 저장 경로, 이름, 인코딩에 대한 정보는 <code>InterfaceInfo</code> 에 저장된 <code>FileTemplate</code> 의 속성들로부터 가져온다.
 * input 데이터의 타입에 따라 파일내용을 텍스트로 쓸 것인지 바이트배열로 쓸 것인지 또는 특정한 포맷으로 작성할 것인지 결정된다.
 * 파일의 내용으로 전달된 데이터의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code> 인 경우
 * <code>recordSeparator</code>, <code>delimiter</code>, <code>qualifier</code>, <code>replacementOfNullValue</code>, <code>replacementOfEmptyValue</code>, <code>addHeader</code>
 * 설정에 따라 파일의 내용이 구성되어 작성된다.
 * <br><br>
 * <i>예시)input 으로 Map[Name:Peter, Age:17, Hobby:Baseball, Phone:(null data), Address:(empty string)] 인 데이터가 이 서비스로 전달되었을 때</i>
 * <pre>
 *  -delimiter = |
 *  -recordSeparator = \n
 *  -qualifier = $
 *  -replacementOfNullValue = ""(empty value)
 *  -replacementOfEmptyValue = ?(empty value)
 * </pre>
 * <i>위와 같이 설정되었다면 파일에는 다음처럼 내용이 쓰여진다.</i>
 * <pre>
 *
 *         $Name$|$Age$|$Hobby$|$Phone$|Address
 *         $Peter$|$17$|$Baseball$|$$|$?$
 * </pre>
 *
 * @see FileTemplate
 * @see ReadFile
 *
 * @author Yuhyun O
 * @version 2024.09.19
 *
 * @Input 생성할 파일의 데이터
 * @InputType
 * <code>byte[]</code><br>
 * <code>String <code><br>
 * <code>Map&lt;String, Object&gt; </code><br>
 * <code>List&lt;Map&lt;String, Object&gt; </code><br>
 *
 * @Output 생성한 파일의 저장 경로(파일명 포함)
 * @OutputType <code>String</code>
 * */
@Slf4j
@Setter
public class WriteFile extends SourceAccessService implements Serializable {
    private static final long serialVersionUID = 1388432570284684150L;
    /**
     * 기본값: true
     * <br>
     * true일 때 파일이 이미 존재하는 경우 덮어쓰기를 한다.
     * false인 경우에는 EOF 부터 내용을 덧붙인다.
     * */
    private boolean overwrite = true;
    /**
     * input으로 전달받은 content가 null 인 경우에 파일을 생성할 것인지에 대한 옵션 (기본값: false)
     * */
    private boolean allowCreateEmptyFile = false;
    /**
     * 기본값: \n (Line Feed)<br>
     * 파일 내용으로 쓰일 Input value의 타입이 <code>List&lt;Map&lt;String, Object&gt;&gt;</code> 즉, <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우
     * 각각의 레코드를 구분할 문자에 대한 설정이다.
     * */
    private String recordSeparator = "\n";
    /**
     * 기본값: | (Pipe, Vertical bar)<br>
     * 각각의 컬럼명과 컬럼의 데이터를 구분할 문자를 의미한다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 Input value의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String delimiter = "|";
    /**
     * 기본값: "" (빈값, empty string)<br>
     * 각각의 컬럼명과 데이터의 의미 단위를 제한해 줄 문자를 의미한다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 Input value의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String qualifier = "";
    /**
     * 기본값: (빈값, empty string)
     * 데이터가 null인 경우 파일에 어떤 문자로 대체하여 기입할 지에 대한 설정이다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 Input value의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String replacementOfNullValue = "";
    /**
     * 기본값: (빈값, empty string)
     * 데이터가 빈 문자열인 경우 파일에 어떤 문자로 대체하여 기입할 지에 대한 설정이다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 Input value의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String replacementOfEmptyValue = "";
    /**
     * 기본값: &lf;<br>
     * 파일의 데이터에 Line Feed (\n) 가 존재하는 경우 대체할 문자에 대한 설정이다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 Input value의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String replacementOfLineFeed = "&lf;";
    /**
     * 기본값: &cr;<br>
     * 파일의 데이터에 Carriage return (\r) 이 존재하는 경우 대체할 문자에 대한 설정이다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 Input value의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String replacementOfCarriageReturn = "&cr;";
    /**
     * 기본값: true<br>
     * 파일의 상단에 컬럼명을 기입할 지에 대한 설정이다.(단 addMetadata = true 인 경우 그 하단에 기입됨)<br>
     * <code>List&lt;Map&lt;String, Object&gt;&gt;</code> 즉,  <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code>의 형태로 input이 전달되었을 때
     * 리스트의 가장 첫번째 인덱스인 Map의 KeySet이 컬럼명으로 사용되고 그 이후 인덱스에서는 이미 추출된 컬럼명으로 데이터를 가져와 내용을 작성하게된다.<br><br>
     * <i>예시)</i>
     * <pre>
     * &lt;![METADATA[
     *         . . .
     * ]]&gt;
     * Column1|Column2|Column3|...    ← Header 부분
     * Value|Value|Value|...
     *
     * </pre><br><br>
     * <i>이 속성은 파일 내용으로 쓰일 Input value의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private boolean addHeader = true;
    /**
     * addHeader = true 인 경우 헤더 컬럼의 정렬 옵션<br>
     * 0 → 정렬없음<br>
     * 1 → 컬럼명 오름차순<br>
     * -1 → 컬럼명 내림차순<br>
     * 위의 값에 해당되지 않는 경우 정렬없음으로 설정된다.
     * */
    private int headerColumnSorting = 0;

    /**
     * 기본값: false<br>
     * 파일의 내용 가장 상단에 파일 인코딩, Parsing 을 위한 정보 등을 기입할 지에 대한 설정이다.
     * 메타데이터에 대한 설정은 input된 데이터의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code> 인 경우에 적용된다.
     * 메타데이터는 다음과 같은 형식으로 작성된다.
     * <pre>
     * &lt;![METADATA[...]]&gt;
     *          ...      ← 파일의 Header 또는 본문 부분
     * </pre>
     * <br>
     * <br>
     * */
    private boolean addMetadata = false;

    /**
     * 기본값: false<br>
     * 파일 내용으로 쓰일 Input value의 타입이 <code>List&lt;Map&lt;String, Object&gt;&gt;</code> 즉, <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 이고
     * <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code>의 데이터 타입이 바이트 배열(<code>byte[]</code>)인 경우 해당 데이터를 문자열로 변환하여 파일에 쓸지 결정하는 옵션이다.<br>
     * handleBinaryToString 속성이 true인 경우 문자열의 인코딩은 <code>InterfaceInfo</code>가 참조하는 <code>FileTemplate</code> 의 charset 또는 이 서비스의 속성인 <code>commonCharset<</code> 의 설정을 따른다.
     * */
    private boolean handleBinaryToString = false;
    /**
     * 기본값: true<br>
     * 파일 내용으로 쓰일 Input value의 타입이 <code>List&lt;Map&lt;String, Object&gt;&gt;</code> 즉, <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 이고
     * <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code>의 데이터 타입이 바이트 배열(<code>byte[]</code>)인 경우 해당 데이터를 바이트 배열 그대로 파일에 쓸지 결정하는 옵션이다. 기본값이 true 이다.<br>
     * 바이너리 데이터가 파일에 작성될 때 데이터는 FileParser.BINARY_DATA_WRAPPER(&lt;![BINARY[...]]&gt;) 안에 쓰여지며 바이트 배열의 각 원소는 white space 로 구분된다.<br>
     * <i>예시)</i>
     * <pre>
     *    byte[] bytes = new String("ABCDEFGHIJKLMNOPQRSTUVWXYZ").getBytes("UTF-8")
     *    → &lt;![BINARY[65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90]]&gt;
     * </pre>
     * */
    private boolean handleBinaryAsItIs = true;

    /**
     * 파일명 앞에 붙일 접두사에 대한 설정<br>
     * <i>
     *     filenamePrefix + 파일명.확장자
     * </i>
     *
     * */
    private String filenamePrefix = null;
    /**
     * 파일명 끝에 붙일 접미사에 대한 설정<br>
     * <i>
     *     파일명 + filenameSuffix
     * </i>
     *
     * */
    private String filenameSuffix = null;

    /**
     * 파일명을 FileTemplate 이 아닌 ServiceContext 의 특정 값을 가져오고자 할 때
     * ServiceContext 에 output 된 파라미터 명을 지정해주면 그 값으로 파일명을 사용하게 된다.
     * 이 설정을 사용하는 경우 filenamePrefix 속성과 filenameSuffix 속성은 무효하다.
     * */
    private String filenameInput = null;

    /**
     * directoryType 속성에 따라 <code>FileTemplate</code>에서 어떤 속성의 값을 생성한 파일의 저장 경로로써 사용할 지 결정된다.<br><br>
     * -기본값: <code>LOCAL_WRITE</code><br>
     * -REMOTE_SEND → <code>FileTemplate#remoteSendDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_RECEIVE → <code>FileTemplate#remoteReceiveDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_TEMP → <code>FileTemplate#remoteTempDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_SUCCESS → <code>FileTemplate#remoteSuccessDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_ERROR → <code>FileTemplate#remoteErrorDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_BACKUP → <code>FileTemplate#remoteBackupDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_MOVE → <code>FileTemplate#remoteMoveDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_COPY → <code>FileTemplate#remoteCopyDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -REMOTE_WRITE → <code>FileTemplate#remoteWriteDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_SEND → <code>FileTemplate#localSendDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_RECEIVE → <code>FileTemplate#localReceiveDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_TEMP → <code>FileTemplate#localTempDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_SUCCESS → <code>FileTemplate#localSuccessDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_ERROR → <code>FileTemplate#localErrorDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_BACKUP → <code>FileTemplate#localBackupDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_MOVE → <code>FileTemplate#localMoveDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_COPY → <code>FileTemplate#localCopyDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * -LOCAL_WRITE → <code>FileTemplate#localWriteDir</code> 을 생성한 파일의 저장 경로로 사용함<br>
     * */
    private DirectoryType directoryType = DirectoryType.LOCAL_WRITE;

    /**
     * 생성될 파일의 인코딩을 지정하는 설정이다.<br>
     * 이 속성이 지정되지 않았을 경우, 이 서비스의 <code>process(ServiceContext)</code> 메소드를 통해 전달된 <code>ServiceContext</code>의 <code>InterfaceInfo</code>가 참조하는
     * <code>FileTemplate</code> 의 <code>charset<</code> 속성을 인코딩으로 사용한다.
     * */
    private Charset commonCharset;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "WriteFile service must have the input parameter in which contain the file data to write");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = info.getSourceNameByAlias(getSourceAlias());
        String txId = ctx.getTxId();

        //input으로 전달된 이동 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);

        if (inputVal == null && !allowCreateEmptyFile) {
            log.debug("[{}]The value of input '{}' is not found. No file paths to move found in context data.", txId, getInput());
            return;
        }

        // outPutDataType 이 dataTypeDataType.FILE 인 경우 InterfaceInfo에서 FileTemplate을 가져와 directoryType 과 일치하는 경로에 파일을 저장함
        FileTemplate template = info.getFileTemplate(srcName);
        String filename = null;

        if (filenameInput == null) {
            filename = template.getFileName(ctx);
            if (filename == null)
                throw new InvalidServiceConfigurationException(this.getClass(), "The FileTemplate's filename configuration is null. interface id: " + info.getInterfaceId());
            if (filenamePrefix != null && !filenamePrefix.isEmpty()) {
                filename = filenamePrefix + filename;
            }
            if (filenameSuffix != null && !filenameSuffix.isEmpty()) {
                filename += filenameSuffix;
            }
        } else {
            Object filenameInputVal = ctx.getContextParam(filenameInput);
            if (filenameInputVal == null)
                throw new InvalidServiceConfigurationException(this.getClass(), "The property filenameInput is exist but there is no value with name '" + filenameInput + "' in the context");
            filename = filenameInputVal.toString();
        }

        Charset charset = null;
        if (commonCharset != null) {
            charset = commonCharset;
        } else {
            charset = template.getCharset();
        }


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
            if (filename.contains(keyBd)) {
                filename = filename.replace(keyBd, value);
            }
        }

        Path path = Paths.get(savePath);

        if (!Files.exists(path)) {
            log.info("[{}]Creating directories \"{}\"",txId, Files.createDirectories(path));
        }


        String contentStr = null;
        byte[] contentBytes = null;
        List<Map<String, Object>> contentListMap = null;
        try {
            if (inputVal != null) { //allowCreateEmptyFile = true 인 경우 현재 위치에서 inputVal이 null 일 수 있기 때문에 null 인지 검증
                if (inputVal instanceof byte[]) {
                    contentBytes = (byte[]) inputVal;

                } else if (inputVal instanceof String) {
                    contentStr = new String(inputVal.toString().getBytes(charset), charset);

                } else if (inputVal instanceof Map) {
                    contentListMap = new ArrayList<>();
                    contentListMap.add((Map<String, Object>) inputVal);

                } else if (inputVal instanceof List) {
                    contentListMap = (List<Map<String, Object>>) inputVal;

                } else {
                    throw new ClassCastException();
                }
            } else {
                contentBytes = new byte[0];
            }
        } catch (ClassCastException ce) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The type of the input parameter value is not contained in [String, byte[], Map<String, Object>, List<Map<String, Object>>]. Inputted value's type: " + inputVal.getClass().getName());
        }

        Path filePath = path.resolve(filename);
        if (overwrite) {
            if (Files.exists(filePath)) {
                log.debug("[{}]Overwriting file ...", txId);
            }
            Files.deleteIfExists(filePath);
        }
        log.info("[{}]Writing file to \"{}\" ...", txId, savePath);

        OutputStream os = null;
        long filesize = 0;
        try {
            //addMetadata 가 true 이고 input 된 데이터의 타입이 Map<String, Object> 또는 List<Map<String, Object>>인 경우에만 메타데이터를 작성한다.
            if (addMetadata && contentListMap != null) {
                log.debug("[{}]Adding metadata to \"{}\"", txId, filePath);
                if (Files.exists(filePath)) {
                    if (Files.size(path) != 0) {
                        throw new IllegalStateException("The file " + filePath + "'s content is exists. Can not write metadata.");
                    }
                } else {
                    Files.createFile(filePath);
                }
                Map<String, Object> metadata = new LinkedHashMap<>();
                metadata.put("add_header", addHeader);
                metadata.put("record_separator", recordSeparator);
                metadata.put("delimiter", delimiter);
                metadata.put("qualifier", qualifier);
                metadata.put("replacement_of_null_value", replacementOfNullValue);
                metadata.put("replacement_of_empty_value", replacementOfEmptyValue);
                metadata.put("replacement_of_line_feed", replacementOfLineFeed);
                metadata.put("replacement_of_carriage_return", replacementOfCarriageReturn);
                metadata.put("handle_binary_as_it_is", handleBinaryAsItIs);
                metadata.put("handle_binary_to_string", handleBinaryToString);
                StringBuilder mdbd = new StringBuilder();
                mdbd.append("<![METADATA[")
                        .append(MessageUtil.mapToJson(metadata, false))
                        .append("]]>\n");

                Files.write(filePath, mdbd.toString().getBytes(charset), StandardOpenOption.APPEND);
                filesize = Files.size(filePath);
            }
            if (contentListMap != null) {
                log.debug("[{}]Content type: formatted text \"{}\"", txId, filePath);
                os = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                filesize += writeFileAsFormattedData(filePath, charset, contentListMap, os);
            } else if (contentStr != null) {
                log.debug("[{}]Content type: text \"{}\"", txId, filePath);
                filesize += writeFile(filePath, charset, contentStr);
            } else {
                log.debug("[{}]Content type: bytes \"{}\"", txId, filePath);
                filesize += writeFile(filePath, charset, contentBytes);
            }
            log.info("[{}]The file was saved at \"{}\"", txId, filePath, savePath);
        } catch (Throwable t) {
            Files.deleteIfExists(filePath);
            log.warn("[{}]An error occurred while writing file to \"{}\". Error file is deleted", txId, savePath);
            throw t;
        } finally {
            if (os != null) {
                os.close();
            }
        }

        if (getOutput() != null) {
            setOutputValue(ctx, filePath.toString());
        }

    }

    /**
     * @param path 
     * 파일이 저장될 경로
     * @param charset
     * 파일의 인코딩
     * @param content
     * 파일에 쓰일 데이터
     * @return 생성된 파일의 크기
     * */
    private long writeFileAsFormattedData(Path path, Charset charset, List<Map<String, Object>> content, OutputStream os) throws IOException {
        if (content.size() == 0) {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            return 0;
        }

        //헤더(컬럼명) 작성 시작
        List<String> columns = new ArrayList<>(content.get(0).keySet());
        if (addHeader) {
            //headerColumnSorting 이 1 또는 -1 인 경우 파일의 헤더 컬럼을 정렬한다.
            if (headerColumnSorting == 1) {
                SortingUtil.sortList(columns, SortingUtil.Sorting.ASC);
            } else if (headerColumnSorting == -1) {
                SortingUtil.sortList(columns, SortingUtil.Sorting.DESC);
            }

            StringBuffer headerBf = new StringBuffer();
            for (String column : columns) {
                headerBf.append(qualifier).append(column).append(qualifier).append(delimiter);
            }
            if (headerBf.length() > 0) {
                headerBf.setLength(headerBf.length() - delimiter.length());
                headerBf.append(recordSeparator);
            }
            os.write(headerBf.toString().getBytes(charset));
        }


        long bytesWritten = 0;

        //파일 본문 작성시작
        for (Map<String, Object> row : content) {
            StringBuffer rowBf = new StringBuffer();

            for (String column : columns) {
                Object objVal = row.get(column);
                String val = null;
                if (objVal == null) {
                    val = replacementOfNullValue;
                } else {
                    Class clazz = objVal.getClass();
                    if (clazz == byte[].class) { //데이터 타입이 바이트 배열인 경우 이 서비스의 설정에 따라 바이트배열이 문자열로 변환되거나 그대로 쓰여진다.
                        if (handleBinaryToString) {
                            val = new String((byte[]) objVal, charset);
                        } else if (handleBinaryAsItIs) {
                            StringBuffer byteBf = new StringBuffer("<![BINARY[");
                            for (byte b : (byte[]) objVal) {
                                byteBf.append(b).append(" ");
                            }
                            if (byteBf.length() > 0) {
                                byteBf.setLength(byteBf.length() - 1);
                            }
                            byteBf.append("]]>");
                            val = byteBf.toString();
                        }
                    } else {
                        val = String.valueOf(objVal);
                    }
                }
                if (val.isEmpty()) {
                    val = replacementOfEmptyValue;
                }
                rowBf.append(qualifier).append(val).append(qualifier).append(delimiter);
            }

            //Carriage Return 과 Line Feed 를 교체하는 과정
            while (true) {
                int crIdx = rowBf.indexOf("\r");
                int lfIdx = rowBf.indexOf("\n");

                if (crIdx != -1) {
                    rowBf.deleteCharAt(crIdx);
                    rowBf.insert(crIdx, replacementOfCarriageReturn);
                    continue;
                }
                if (lfIdx != -1) {
                    rowBf.deleteCharAt(lfIdx);
                    rowBf.insert(lfIdx, replacementOfLineFeed);
                    continue;
                }
                if (crIdx == -1 && lfIdx == -1) {
                    break;
                }
            }

            //문자열의 끝부분 불필요한 delimiter를 제거한 뒤 각 라인을 구분할 수 있는 구분자를 추가한다.
            if (rowBf.length() > 0) {
                rowBf.setLength(rowBf.length() - delimiter.length());
                rowBf.append(recordSeparator);
            }

            byte[] dataToWrite = rowBf.toString().getBytes(charset);
            os.write(dataToWrite);
            bytesWritten += dataToWrite.length;
        }

        return bytesWritten;
    }

    /**
     * @param path
     * 파일이 저장될 경로
     * @param charset
     * 파일의 인코딩
     * @param content
     * 파일에 쓰일 데이터
     * @return 생성된 파일의 크기
     * */
    private long writeFile(Path path, Charset charset, String content) throws IOException {
        byte[] contentBytes = content.getBytes(charset);
        return writeFile(path, charset, contentBytes);
    }

    /**
     * @param path
     * 파일이 저장될 경로
     * @param charset
     * 파일의 인코딩
     * @param content
     * 파일에 쓰일 데이터
     * @return 생성된 파일의 크기
     * */
    private long writeFile(Path path, Charset charset, byte[] content) throws IOException {
        Files.write(path, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return content.length;
    }




    public void setDelimiter(String delimiter) {
        /*if (delimiter.equals(recordSeparator))
            throw new IllegalArgumentException("The delimiter '" + delimiter + "' must be different with recordSeparator '" + recordSeparator + "'");
        if (delimiter.equals(qualifier))
            throw new IllegalArgumentException("The delimiter '" + delimiter + "' must be different with qualifier '" + qualifier + "'");
        if (delimiter.equals(replacementOfNullValue))
            throw new IllegalArgumentException("The delimiter '" + delimiter + "' must be different with replacementOfNullValue '" + replacementOfNullValue + "'");
        if (delimiter.equals(replacementOfEmptyValue))
            throw new IllegalArgumentException("The delimiter '" + delimiter + "' must be different with replacementOfEmptyValue '" + replacementOfEmptyValue + "'");
        if (delimiter.equals(replacementOfLineFeed))
            throw new IllegalArgumentException("The delimiter '" + delimiter + "' must be different with replacementOfLineFeed '" + replacementOfLineFeed + "'");
        if (delimiter.equals(replacementOfCarriageReturn))
            throw new IllegalArgumentException("The delimiter '" + delimiter + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");*/
        if (handleBinaryAsItIs && FileParser.BINARY_DATA_WRAPPER.contains(delimiter))
            throw new IllegalArgumentException("The delimiter '" + delimiter + "' must not be contained in FileParser.BINARY_DATA_WRAPPER '" + FileParser.BINARY_DATA_WRAPPER + "'");

        this.delimiter = delimiter;
    }

    public void setRecordSeparator(String recordSeparator) {
        /*if (recordSeparator.equals(delimiter))
            throw new IllegalArgumentException("The recordSeparator '" + recordSeparator + "' must be different with delimiter '" + delimiter + "'");
        if (recordSeparator.equals(qualifier))
            throw new IllegalArgumentException("The recordSeparator '" + recordSeparator + "' must be different with qualifier '" + qualifier + "'");
        if (recordSeparator.equals(replacementOfNullValue))
            throw new IllegalArgumentException("The recordSeparator '" + recordSeparator + "' must be different with replacementOfNullValue '" + replacementOfNullValue + "'");
        if (recordSeparator.equals(replacementOfEmptyValue))
            throw new IllegalArgumentException("The recordSeparator '" + recordSeparator + "' must be different with replacementOfEmptyValue '" + replacementOfEmptyValue + "'");
        if (recordSeparator.equals(replacementOfLineFeed))
            throw new IllegalArgumentException("The recordSeparator '" + recordSeparator + "' must be different with replacementOfLineFeed '" + replacementOfLineFeed + "'");
        if (recordSeparator.equals(replacementOfCarriageReturn))
            throw new IllegalArgumentException("The recordSeparator '" + recordSeparator + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");*/
        if (handleBinaryAsItIs && FileParser.BINARY_DATA_WRAPPER.contains(recordSeparator))
            throw new IllegalArgumentException("The recordSeparator '" + recordSeparator + "' must not be contained in FileParser.BINARY_DATA_WRAPPER '" + FileParser.BINARY_DATA_WRAPPER + "'");

        this.recordSeparator = recordSeparator;
    }

    public void setQualifier(String qualifier) {
        /*if (qualifier.equals(delimiter))
            throw new IllegalArgumentException("The qualifier '" + qualifier + "' must be different with delimiter '" + delimiter + "'");
        if (qualifier.equals(recordSeparator))
            throw new IllegalArgumentException("The qualifier '" + qualifier + "' must be different with recordSeparator '" + recordSeparator + "'");
        if (qualifier.equals(replacementOfNullValue))
            throw new IllegalArgumentException("The qualifier '" + qualifier + "' must be different with replacementOfNullValue '" + replacementOfNullValue + "'");
        if (qualifier.equals(replacementOfEmptyValue))
            throw new IllegalArgumentException("The qualifier '" + qualifier + "' must be different with replacementOfEmptyValue '" + replacementOfEmptyValue + "'");
        if (qualifier.equals(replacementOfLineFeed))
            throw new IllegalArgumentException("The qualifier '" + qualifier + "' must be different with replacementOfLineFeed '" + replacementOfLineFeed + "'");
        if (qualifier.equals(replacementOfCarriageReturn))
            throw new IllegalArgumentException("The qualifier '" + qualifier + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");*/
        if (handleBinaryAsItIs && FileParser.BINARY_DATA_WRAPPER.contains(qualifier))
            throw new IllegalArgumentException("The qualifier '" + qualifier + "' must not be contained in FileParser.BINARY_DATA_WRAPPER '" + FileParser.BINARY_DATA_WRAPPER + "'");

        this.qualifier = qualifier;
    }

    public void setReplacementOfNullValue(String replacementOfNullValue) {
        /*if (replacementOfNullValue.equals(delimiter))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must be different with delimiter '" + delimiter + "'");
        if (replacementOfNullValue.equals(recordSeparator))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must be different with recordSeparator '" + recordSeparator + "'");
        if (replacementOfNullValue.equals(qualifier))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must be different with qualifier '" + qualifier + "'");
        if (replacementOfNullValue.equals(replacementOfLineFeed))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must be different with replacementOfLineFeed '" + replacementOfLineFeed + "'");
        if (replacementOfNullValue.equals(replacementOfCarriageReturn))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");*/
        if (handleBinaryAsItIs && FileParser.BINARY_DATA_WRAPPER.contains(replacementOfNullValue))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must not be contained in FileParser.BINARY_DATA_WRAPPER '" + FileParser.BINARY_DATA_WRAPPER + "'");

        this.replacementOfNullValue = replacementOfNullValue;
    }

    public void setReplacementOfEmptyValue(String replacementOfEmptyValue) {
        /*if (replacementOfEmptyValue.equals(delimiter))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must be different with delimiter '" + delimiter + "'");
        if (replacementOfEmptyValue.equals(recordSeparator))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must be different with recordSeparator '" + recordSeparator + "'");
        if (replacementOfEmptyValue.equals(qualifier))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must be different with qualifier '" + qualifier + "'");
        if (replacementOfEmptyValue.equals(replacementOfLineFeed))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must be different with replacementOfLineFeed '" + replacementOfLineFeed + "'");
        if (replacementOfEmptyValue.equals(replacementOfCarriageReturn))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");*/
        if (handleBinaryAsItIs && FileParser.BINARY_DATA_WRAPPER.contains(replacementOfEmptyValue))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must not be contained in FileParser.BINARY_DATA_WRAPPER '" + FileParser.BINARY_DATA_WRAPPER + "'");

        this.replacementOfEmptyValue = replacementOfEmptyValue;
    }

    public void setReplacementOfLineFeed(String replacementOfLineFeed) {
        /*if (replacementOfLineFeed.equals(delimiter))
            throw new IllegalArgumentException("The replacementOfLineFeed '" + replacementOfLineFeed + "' must be different with delimiter '" + delimiter + "'");
        if (replacementOfLineFeed.equals(recordSeparator))
            throw new IllegalArgumentException("The replacementOfLineFeed '" + replacementOfLineFeed + "' must be different with recordSeparator '" + recordSeparator + "'");
        if (replacementOfLineFeed.equals(replacementOfNullValue))
            throw new IllegalArgumentException("The replacementOfLineFeed '" + replacementOfLineFeed + "' must be different with replacementOfNullValue '" + replacementOfNullValue + "'");
        if (replacementOfLineFeed.equals(replacementOfEmptyValue))
            throw new IllegalArgumentException("The replacementOfLineFeed '" + replacementOfLineFeed + "' must be different with replacementOfEmptyValue '" + replacementOfEmptyValue + "'");
        if (replacementOfLineFeed.equals(qualifier))
            throw new IllegalArgumentException("The replacementOfLineFeed '" + replacementOfLineFeed + "' must be different with qualifier '" + qualifier + "'");
        if (replacementOfLineFeed.equals(replacementOfCarriageReturn))
            throw new IllegalArgumentException("The replacementOfLineFeed '" + replacementOfLineFeed + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");*/
        if (handleBinaryAsItIs && FileParser.BINARY_DATA_WRAPPER.contains(replacementOfLineFeed))
            throw new IllegalArgumentException("The replacementOfLineFeed '" + replacementOfLineFeed + "' must not be contained in FileParser.BINARY_DATA_WRAPPER '" + FileParser.BINARY_DATA_WRAPPER + "'");

        this.replacementOfLineFeed = replacementOfLineFeed;
    }

    public void setReplacementOfCarriageReturn(String replacementOfCarriageReturn) {
        /*if (replacementOfCarriageReturn.equals(delimiter))
            throw new IllegalArgumentException("The replacementOfCarriageReturn '" + replacementOfCarriageReturn + "' must be different with delimiter '" + delimiter + "'");
        if (replacementOfCarriageReturn.equals(recordSeparator))
            throw new IllegalArgumentException("The replacementOfCarriageReturn '" + replacementOfCarriageReturn + "' must be different with recordSeparator '" + recordSeparator + "'");
        if (replacementOfCarriageReturn.equals(replacementOfNullValue))
            throw new IllegalArgumentException("The replacementOfCarriageReturn '" + replacementOfCarriageReturn + "' must be different with replacementOfNullValue '" + replacementOfNullValue + "'");
        if (replacementOfCarriageReturn.equals(replacementOfEmptyValue))
            throw new IllegalArgumentException("The replacementOfCarriageReturn '" + replacementOfCarriageReturn + "' must be different with replacementOfEmptyValue '" + replacementOfEmptyValue + "'");
        if (replacementOfCarriageReturn.equals(qualifier))
            throw new IllegalArgumentException("The replacementOfCarriageReturn '" + replacementOfCarriageReturn + "' must be different with qualifier '" + qualifier + "'");
        if (replacementOfCarriageReturn.equals(replacementOfLineFeed))
            throw new IllegalArgumentException("The replacementOfCarriageReturn '" + replacementOfCarriageReturn + "' must be different with replacementOfLineFeed '" + replacementOfLineFeed + "'");*/
        if (handleBinaryAsItIs && FileParser.BINARY_DATA_WRAPPER.contains(replacementOfCarriageReturn))
            throw new IllegalArgumentException("The replacementOfCarriageReturn '" + replacementOfCarriageReturn + "' must not be contained in FileParser.BINARY_DATA_WRAPPER '" + FileParser.BINARY_DATA_WRAPPER + "'");

        this.replacementOfCarriageReturn = replacementOfCarriageReturn;
    }

    public void setAddHeader(boolean addHeader) {
        this.addHeader = addHeader;
    }

    public void setHandleBinaryToString(boolean handleBinaryToString) {
        handleBinaryAsItIs = !handleBinaryToString;
        this.handleBinaryToString = handleBinaryToString;
    }

    public void setHandleBinaryAsItIs(boolean handleBinaryAsItIs) {
        handleBinaryToString = !handleBinaryAsItIs;
        this.handleBinaryAsItIs = handleBinaryAsItIs;
    }

    public void setHeaderColumnSorting(int headerColumnSorting) {
        switch (headerColumnSorting) {
            case -1 : case 0 : case 1 : this.headerColumnSorting = headerColumnSorting;
            default: this.headerColumnSorting = 0;
        }
        this.headerColumnSorting = headerColumnSorting;
    }
}
