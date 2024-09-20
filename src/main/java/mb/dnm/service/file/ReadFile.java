package mb.dnm.service.file;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.code.DataType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일의 데이터를 읽는다.<br>
 *
 *
 * @see FileTemplate
 * @see WriteFile
 *
 * @author Yuhyun O
 * @version 2024.09.20
 *
 * @Input 읽을 파일의 경로
 * @InputType
 * <code>String</code><br>
 * <code>java.nio.file.Path</code><br>
 * <code>java.io.File</code>
 *
 * @Output 읽은 파일의 데이터
 * @OutputType
 * <code>byte[]</code><br>
 * <code>String <code><br>
 * <code>Map&lt;String, Object&gt; </code><br>
 * <code>List&lt;Map&lt;String, Object&gt; </code><br>
 * */
@Slf4j
@Setter
public class ReadFile extends SourceAccessService {

    /**
     * 읽을 파일의 인코딩을 지정하는 설정이다.<br>
     * 이 속성이 지정되지 않았을 경우, 이 서비스의 <code>process(ServiceContext)</code> 메소드를 통해 전달된 <code>ServiceContext</code>의 <code>InterfaceInfo</code>가 참조하는
     * <code>FileTemplate</code> 의 <code>charset<</code> 속성을 인코딩으로 사용한다.<br>
     * <code>commonCharset</code>을 등록하는 경우 모든 인터페이스의 인코딩은 이 서비스의 설정을 따른다.
     * */
    private Charset commonCharset;

    /**
     * 파일의 내용을 읽은 뒤 어떤 타입으로 output을 할 지에 대한 설정이다.<br>
     * 이 속성이 지정되지 않았을 경우, 이 서비스의 <code>process(ServiceContext)</code> 메소드를 통해 전달된 <code>ServiceContext</code>의 <code>InterfaceInfo</code>가 참조하는
     * <code>FileTemplate</code> 의 <code>dataType<</code> 속성을 사용한다.<br><br>
     * <i>-설정 가능한 타입과 설명<br>
     *     &nbsp;<code>BYTE_ARRAY</code>: 파일의 데이터를 읽은 뒤 <code>byte[]</code>로 output 한다.<br>
     *     &nbsp;<code>STRING</code>: 파일의 데이터를 읽은 뒤 <code>String</code>으로 output 한다.<br>
     *     &nbsp;<code>LIST_OF_MAP</code>: 파일의 데이터를 정해진 형식대로 읽은 뒤 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>로 output 한다.<br>
     * </i>
     * <br>
     * <code>outputDataType</code>을 등록하는 경우 모든 인터페이스는 이 서비스의 설정을 따른다.
     * */
    private DataType outputDataType;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "ReadFile service must have the input parameter in which contain the file path to read");
        }

        InterfaceInfo info = ctx.getInfo();
        String srcName = info.getSourceNameByAlias(getSourceAlias());
        String txId = ctx.getTxId();

        //input으로 전달된 이동 대상 파일에 대한 정보의 타입을 검증하고 이 서비스에서 사용되는 공통된 형식으로 맞춰주는 과정
        Object inputVal = getInputValue(ctx);

        if (inputVal == null) {
            log.debug("[{}]The value of input '{}' is not found. No file paths to read found in context data.", txId, getInput());
            return;
        }

        Path readFilePath = null;
        try {
            if (inputVal instanceof String) {
                readFilePath = Paths.get((String) inputVal);
            } else if (inputVal instanceof Path) {
                readFilePath = (Path) inputVal;
            } else if (inputVal instanceof File) {
                readFilePath = ((File) inputVal).toPath();
            } else {
                throw new ClassCastException();
            }
        } catch (ClassCastException ce) {
            throw new InvalidServiceConfigurationException(this.getClass(), "The type of the input parameter value is not contained in [String, byte[], Map<String, Object>, List<Map<String, Object>>]. Inputted value's type: " + inputVal.getClass().getName());
        }

        if (!Files.exists(readFilePath)) {
            throw new FileNotFoundException("The file \"" + readFilePath + "\" is not found. Can not read the file.");
        }

        FileTemplate template = info.getFileTemplate(srcName);
        Charset charset = null;
        if (commonCharset != null) {
            charset = commonCharset;
        } else {
            charset = template.getCharset();
        }


    }

    public void setOutputDataType(DataType outputDataType) {
        switch (outputDataType) {
            case STRING: case BYTE_ARRAY: case LIST_OF_MAP: this.outputDataType = outputDataType; break;
            default: throw new IllegalArgumentException("Unsupported output data type: " + outputDataType);
        }
    }

}
