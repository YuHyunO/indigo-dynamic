package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileList;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.code.DirectoryType;
import mb.dnm.code.FileContentType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * 지정된 경로에 파일을 생성한다.
 * <br>
 * 생성할
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
 *
 * @Output 생성한 파일의 저장 경로(파일명 포함)
 * @OutputType <code>String</code>
 * @ErrorOutput 생성 실패한 파일명
 * @OutputType <code>String</code>
 * */
@Slf4j
@Setter
public class WriteFile extends SourceAccessService {
    /**
     * input으로 전달받은 content가 null 이거나 내용이 없는 경우에도 파일을 생성할 것인지에 대한 옵션 (기본값: false)
     * */
    private boolean allowCreateEmptyFile = false;
    /**
     * 기본값: false
     * */
    private boolean ignoreErrorFile = false;

    /**
     * 기본값: StandardOpenOption.APPEND<br>
     * 파일을 write 할 때의 옵션
     *
     * @see StandardOpenOption
     * */
    private StandardOpenOption writeOption = StandardOpenOption.APPEND;
    /**
     * 기본값: \n (Line Feed)<br>
     * 파일 내용으로 쓰일 데이터의 타입이 <code>List&lt;Map&lt;String, Object&gt;&gt;</code> 즉, <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우
     * 각각의 레코드를 구분할 문자에 대한 설정이다.
     * */
    private String recordSeparator = "\n";
    /**
     * 기본값: | (Pipe, Vertical bar)<br>
     * 각각의 컬럼명과 컬럼의 데이터를 구분할 문자를 의미한다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 데이터의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String delimiter = "|";
    /**
     * 기본값: "" (빈값, empty string)<br>
     * 각각의 컬럼명과 데이터의 의미 단위를 제한해 줄 문자를 의미한다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 데이터의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String qualifier = "";
    /**
     * 기본값: (빈값, empty string)
     * 데이터가 null인 경우 파일에 어떤 문자로 대체하여 기입할 지에 대한 설정이다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 데이터의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String replacementOfNullValue = "";
    /**
     * 기본값: (빈값, empty string)
     * 데이터가 빈 문자열인 경우 파일에 어떤 문자로 대체하여 기입할 지에 대한 설정이다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 데이터의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String replacementOfEmptyValue = "";
    /**
     * 기본값: (/LF/)<br>
     * 파일의 데이터에 Line Feed (\n) 가 존재하는 경우 대체할 문자에 대한 설정이다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 데이터의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String replacementOfLineFeed = "(/LF/)";
    /**
     * 기본값: (/CR/)<br>
     * 파일의 데이터에 Carriage return (\r) 이 존재하는 경우 대체할 문자에 대한 설정이다.<br><br>
     * <i>이 속성은 파일 내용으로 쓰일 데이터의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private String replacementOfCarriageReturn = "(/CR/)";
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
     * <i>이 속성은 파일 내용으로 쓰일 데이터의 타입이 <code>Map&lt;String, Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>
     * 즉, <code>Map&lt;컬럼명, 데이터&gt;</code> 또는 <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code> 인 경우에만 유효하다</i>
     * */
    private boolean addHeader = true;
    /**
     * 기본값: false<br>
     * 파일의 내용 가장 상단에 파일 인코딩, Parsing 을 위한 정보 등을 기입할 지에 대한 설정이다.
     * 메타데이터에 대한 설정은 input된 데이터의 타입과 무관하게 적용될 수 있다.
     * 메타데이터는 다음과 같은 형식으로 작성된다.
     * <pre>
     * &lt;![METADATA[{
     *     "encoding": "UTF-8",
     *     "recordSeparator": "\n",
     *     "qualifier": "",
     *     "replacementOfNullValue": "",
                       .
                       .
                       .
     
     *     "replacementOfCarriageReturn": "(/CR/)",
     *     "addHeader": true
     * }]]&gt;
     *          ...      ← 파일의 Header 또는 본문 부분
     * </pre>
     * */
    private boolean addMetadata = false;

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

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "WriteFiles service must have the input parameter in which contain the files to move");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = info.getSourceNameByAlias(getSourceAlias());
        String txId = ctx.getTxId();

        //input으로 전달된 이동 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);

        if (inputVal == null && !allowCreateEmptyFile) {
            log.debug("The value of input '{}' is not found. No file paths to move found in context data.", getInput());
            return;
        }

        // outPutDataType 이 dataTypeDataType.FILE 인 경우 InterfaceInfo에서 FileTemplate을 가져와 directoryType 과 일치하는 경로에 파일을 저장함
        FileTemplate template = info.getFileTemplate(srcName);
        String filename = template.getFileName(ctx);
        FileContentType contentType = template.getContentType();
        Charset charset = template.getCharset();


        String savePath = null;
        if (template == null)
            throw new InvalidServiceConfigurationException(this.getClass(), "The File template with name '" + srcName + "' of the interface '" + info.getInterfaceId() + "' is null.");
        savePath = template.getFilePath(directoryType);
        if (savePath == null)
            throw new InvalidServiceConfigurationException(this.getClass(), "The value of " + directoryType + " of the template with name '" + srcName + "' is null");
        if (savePath.contains("@{if_id}")) {
            savePath = savePath.replace("@{if_id}", ctx.getInterfaceId());
        }
        //### PlaceHolder mapping 을 적용할 것

        Path path = Paths.get(savePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }


        String contentStr = null;
        byte[] contentBytes = null;
        List<Map<String, Object>> contentListMap = null;
        try {
            if (inputVal != null) {
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

        String successFilePath = null;
        if (contentListMap != null) {
            successFilePath = writeFileAsFormattedData(filename, savePath, charset, contentListMap, contentType);
        } else if (contentStr != null) {
            successFilePath = writeFileAsText(filename, savePath, charset, contentStr, contentType);
        } else {
            successFilePath = writeFileAsBytes(filename, savePath, charset, contentBytes, contentType);
        }

        if (getOutput() != null) {
            setOutputValue(ctx, successFilePath);
        }

    }

    public String writeFileAsFormattedData(String filename, String savePath, Charset charset, List<Map<String, Object>> content, FileContentType contentType) throws Throwable {


        return null;
    }

    public String writeFileAsText(String filename, String savePath, Charset charset, String content, FileContentType contentType) throws Throwable {


        return null;
    }

    public String writeFileAsBytes(String filename, String savePath, Charset charset, byte[] content, FileContentType contentType) throws Throwable {


        return null;
    }




    public void setDelimiter(String delimiter) {
        if (delimiter.equals(recordSeparator))
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
            throw new IllegalArgumentException("The delimiter '" + delimiter + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");

        this.delimiter = delimiter;
    }

    public void setRecordSeparator(String recordSeparator) {
        if (recordSeparator.equals(delimiter))
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
            throw new IllegalArgumentException("The recordSeparator '" + recordSeparator + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");

        this.recordSeparator = recordSeparator;
    }

    public void setQualifier(String qualifier) {
        if (qualifier.equals(delimiter))
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
            throw new IllegalArgumentException("The qualifier '" + qualifier + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");

        this.qualifier = qualifier;
    }

    public void setReplacementOfNullValue(String replacementOfNullValue) {
        if (replacementOfNullValue.equals(delimiter))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must be different with delimiter '" + delimiter + "'");
        if (replacementOfNullValue.equals(recordSeparator))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must be different with recordSeparator '" + recordSeparator + "'");
        if (replacementOfNullValue.equals(qualifier))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must be different with qualifier '" + qualifier + "'");
        if (replacementOfNullValue.equals(replacementOfLineFeed))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must be different with replacementOfLineFeed '" + replacementOfLineFeed + "'");
        if (replacementOfNullValue.equals(replacementOfCarriageReturn))
            throw new IllegalArgumentException("The replacementOfNullValue '" + replacementOfNullValue + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");

        this.replacementOfNullValue = replacementOfNullValue;
    }

    public void setReplacementOfEmptyValue(String replacementOfEmptyValue) {
        if (replacementOfEmptyValue.equals(delimiter))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must be different with delimiter '" + delimiter + "'");
        if (replacementOfEmptyValue.equals(recordSeparator))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must be different with recordSeparator '" + recordSeparator + "'");
        if (replacementOfEmptyValue.equals(qualifier))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must be different with qualifier '" + qualifier + "'");
        if (replacementOfEmptyValue.equals(replacementOfLineFeed))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must be different with replacementOfLineFeed '" + replacementOfLineFeed + "'");
        if (replacementOfEmptyValue.equals(replacementOfCarriageReturn))
            throw new IllegalArgumentException("The replacementOfEmptyValue '" + replacementOfEmptyValue + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");

        this.replacementOfEmptyValue = replacementOfEmptyValue;
    }

    public void setReplacementOfLineFeed(String replacementOfLineFeed) {
        if (replacementOfLineFeed.equals(delimiter))
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
            throw new IllegalArgumentException("The replacementOfLineFeed '" + replacementOfLineFeed + "' must be different with replacementOfCarriageReturn '" + replacementOfCarriageReturn + "'");

        this.replacementOfLineFeed = replacementOfLineFeed;
    }

    public void setReplacementOfCarriageReturn(String replacementOfCarriageReturn) {
        if (replacementOfCarriageReturn.equals(delimiter))
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
            throw new IllegalArgumentException("The replacementOfCarriageReturn '" + replacementOfCarriageReturn + "' must be different with replacementOfLineFeed '" + replacementOfLineFeed + "'");

        this.replacementOfCarriageReturn = replacementOfCarriageReturn;
    }

    public void setAddHeader(boolean addHeader) {
        this.addHeader = addHeader;
    }
}
