package mb.dnm.service.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.access.file.FileParser;
import mb.dnm.access.file.FileParserTemplate;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.code.DataType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.SourceAccessService;
import mb.dnm.storage.InterfaceInfo;
import mb.dnm.util.MessageUtil;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * 파일의 데이터를 읽는다.<br>
 * 파일을 읽은 뒤 설정된 <code>FileTemplate.dataType</code> 또는 <code>outputDataType</code> 에 따라 output 되는 데이터의 타입이 달라진다.<br>
 * <i>
 *     <code>BYTE_ARRAY</code> 인 경우<br>
 *     &nbsp;1. Input으로 전달된 파일경로에 있는 파일을 byte[]로 읽는다. (파일이 없는 경우 FileNotFoundException 발생)<br>
 *     &nbsp;2. 읽은 byte[]를 output 한다.<br><br>
 *
 *     <code>STRING</code> 인 경우<br>
 *     &nbsp;1. Input으로 전달된 파일경로에 있는 파일을 byte[]로 읽는다. (파일이 없는 경우 FileNotFoundException 발생)<br>
 *     &nbsp;2. 읽은 byte[]를 정해진 charset으로 인코딩하여 문자열로 변환한 뒤 output 한다.<br><br>
 *
 *     <code>LIST_OF_MAP</code> 인 경우<br>
 *     &nbsp;1. Input으로 전달된 파일경로에 있는 파일의 IO Stream 을 open 한다. (파일이 없는 경우 FileNotFoundException 발생)<br>
 *     &nbsp;2. 아래에 속성에 따라 파일의 데이터를 Parsing 한다.(<code>metadataExist</code> 가 true 인 경우 메타데이터의 속성대로 Parsing 한다.)<br>
 *     <code>recordSeparator</code>, <code>delimiter</code>, <code>qualifier</code>, <code>replacementOfNullValue</code>,
 *     <code>replacementOfEmptyValue</code>, <code>replacementOfLineFeed</code>, <code>replacementOfCarriageReturn</code>
 *     , <code>headerExist</code>, <code>handleBinaryToString</code>, <code>handleBinaryAsItIs</code><br>
 *     &nbsp;3. <code>headerExist</code>가 true 인 경우 <code>List&lt;List&lt;Object&gt;</code> 를 output 하고, false 인 경우 <code>List&lt;Map&lt;String, Object&gt;&gt;</code> 를 output 한다.<br><br>
 *
 * </i>
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
 * <code>List&lt;List&lt;Object&gt; </code><br>
 * <code>List&lt;Map&lt;String, Object&gt; </code><br>
 *
 * */
@Slf4j
@Setter
public class ReadFile extends SourceAccessService implements Serializable {
    private static final long serialVersionUID = -6076772516465372292L;
    /**
     * 읽을 파일의 인코딩을 지정하는 설정이다.<br>
     * 이 속성이 지정되지 않았을 경우, 이 서비스의 <code>process(ServiceContext)</code> 메소드를 통해 전달된 <code>ServiceContext</code>의 <code>InterfaceInfo</code>가 참조하는
     * <code>FileTemplate</code> 의 <code>charset<</code> 속성을 인코딩으로 사용한다.<br>
     * <code>commonCharset</code>을 등록하는 경우 모든 인터페이스의 인코딩은 이 서비스의 설정을 따른다.
     * */
    private Charset commonCharset;

    /**
     * 기본값: null<br>
     *
     * 파일의 내용을 읽은 뒤 어떤 타입으로 output을 할 지에 대한 설정이다.<br>
     * 이 속성이 지정되지 않았을 경우, 이 서비스의 <code>process(ServiceContext)</code> 메소드를 통해 전달된 <code>ServiceContext</code>의 <code>InterfaceInfo</code>가 참조하는
     * <code>FileTemplate</code> 의 <code>dataType<</code> 속성을 사용한다.<br><br>
     * <i>-설정 가능한 타입과 설명<br>
     *     &nbsp;<code>BYTE_ARRAY</code>: 파일의 데이터를 읽은 뒤 <code>byte[]</code>로 output 한다.<br>
     *     &nbsp;<code>STRING</code>: 파일의 데이터를 설정된 charset 으로 읽은 뒤 <code>String</code>으로 output 한다.<br>
     *     &nbsp;<code>PARSED_TEXT</code>: 파일의 데이터를 설정된 charset 으로 읽은 뒤 정해진 형식대로 parsing 하여 <code>List&lt;List&lt;Object&gt;</code> 또는 <code>List&lt;Map&lt;String, Object&gt;&gt;</code>로 output 한다.<br>
     *     &nbsp;&nbsp;-<code>headerExist</code>가 false 인 경우 → <code>List&lt;List&lt;Object&gt;</code> 즉, <code>List&lt;List&lt;데이터&gt;</code><br>
     *     &nbsp;&nbsp;-<code>headerExist</code>가 true 인 경우 → <code>List&lt;Map&lt;String, Object&gt;&gt;</code> 즉, <code>List&lt;Map&lt;컬럼명, 데이터&gt;&gt;</code>
     * </i>
     * <br>
     * <br>
     * <code>outputDataType</code>을 설정하는 경우 모든 인터페이스는 이 서비스의 설정을 따른다.
     * */
    private DataType outputDataType;

    /**
     * 기본값: \n (Line Feed)<br>
     * 파일 내용을 읽은 뒤 output할 데이터의 타입이 PARSED_TEXT 인 경우
     * 파일에서 각각의 레코드를 구분하는 문자가 어떤 것인지에 대한 설정이다.
     * */
    private String recordSeparator = "\n";
    /**
     * 기본값: | (Pipe, Vertical bar)<br>
     * 파일 내용을 읽은 뒤 output할 데이터의 타입이 PARSED_TEXT 인 경우
     * 파일에서 각각의 컬럼명과 컬럼의 데이터를 구분하는 문자가 어떤 것인지에 대한 설정이다.
     * */
    private String delimiter = "|";
    /**
     * 기본값: "" (빈값, empty string)<br>
     * 파일 내용을 읽은 뒤 output할 데이터의 타입이 PARSED_TEXT 인 경우
     * 파일에서 각각의 컬럼명과 데이터의 의미 단위를 구분해주는 문자가 어떤 것인지에 대한 설정이다.
     * */
    private String qualifier = "";
    /**
     * 기본값: (빈값, empty string)
     * 파일 내용을 읽은 뒤 output할 데이터의 타입이 PARSED_TEXT 인 경우
     * null인 데이터가 파일에 어떤 문자로 대체되어 기입되어있는지에 대한 설정이다.
     * */
    private String replacementOfNullValue = "";
    /**
     * 기본값: (빈값, empty string)
     * 파일 내용을 읽은 뒤 output할 데이터의 타입이 PARSED_TEXT 인 경우
     * 빈 문자열이 파일에 어떤 문자로 대체되어 기입되어있는지에 대한 설정이다.
     * */
    private String replacementOfEmptyValue = "";
    /**
     * 기본값: &cr;<br>
     * 파일 내용을 읽은 뒤 output할 데이터의 타입이 PARSED_TEXT 인 경우
     * 파일 내용 중 Line Feed (\n) 가 어떤 문자로 대체되어 기입되어있는지에 대한 설정이다.
     * */
    private String replacementOfLineFeed = "&cr;";
    /**
     * 기본값: &lf;<br>
     * 파일 내용을 읽은 뒤 output할 데이터의 타입이 PARSED_TEXT 인 경우
     * 파일 내용 중  Carriage return (\r) 이 어떤 문자로 대체되어 기입되어있는지에 대한 설정이다.
     * */
    private String replacementOfCarriageReturn = "&lf;";
    /**
     * 기본값: true<br>
     * 파일 내용을 읽은 뒤 output할 데이터의 타입이 PARSED_TEXT 인 경우
     * 파일에 컬럼명이 기입되어 있는지에 대한 설정이다.
     * */
    private boolean headerExist = true;

    /**
     * 기본값: false<br>
     * 파일 내용을 읽은 뒤 output할 데이터의 타입이 PARSED_TEXT 인 경우
     * 파일의 내용 가장 상단에 파일 인코딩, Parsing 을 위한 정보 등이 기입되어 있는지에 대한 설정이다.
     * 이 속성이 true인 경우 파일 내용에서 메타데이터를 분석하여 그 정보를 기반으로 파일을 Parsing 한다.<br>
     * 메타데이터의 형식은 <code>mb.dnm.service.file.WriteFile</code> 의 형식을 따르며, metadataExist = true 이지만
     * 메타데이터의 속성 중 특정 값이 하나라도 없는 경우 Exception이 발생한다.
     *
     * @see WriteFile
     * */
    private boolean metadataExist = false;

    /**
     * 기본값: true<br>
     * 파일 내용을 읽은 뒤 output할 데이터의 타입이 PARSED_TEXT 인 경우
     * 파일 내용 중 BINARY_DATA_WRAPPER(&lt;![BINARY[...]]&gt;) 안에 쓰여진 내용을 byte array로 Parsing 할 것인지에 대한 옵션이다.
     * <pre>
     *    &lt;![BINARY[65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90]]&gt;
     *    → byte[] bytes = {65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90};
     * </pre>
     * */
    private boolean handleBinaryData = true;


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
            throw new InvalidServiceConfigurationException(this.getClass(), "The type of the input parameter value is not contained in [String, Path, File]. Inputted value's type: " + inputVal.getClass().getName());
        }

        if (!Files.exists(readFilePath)) {
            throw new FileNotFoundException("The file \"" + readFilePath + "\" is not found. Can not read the file.");
        }

        FileTemplate template = info.getFileTemplate(srcName);
        DataType outputType = null;
        if (outputDataType != null) {
            outputType = outputDataType;
        } else {
            outputType = template.getDataType();
        }

        Charset charset = null;
        if (commonCharset != null) {
            charset = commonCharset;
        } else {
            charset = template.getCharset();
        }

        Object fileData = null;
        try {

            switch (outputType) {
                case BYTE_ARRAY: {
                    log.info("[{}]Reading file \"{}\" to 'byte array' ...", txId, readFilePath);
                    fileData = Files.readAllBytes(readFilePath);
                    log.info("[{}]Read complete. Size: {} bytes", txId, ((byte[]) fileData).length);
                    break;
                }
                case STRING: {
                    log.info("[{}]Reading file \"{}\" to 'String' using charset '{}' ...", txId, readFilePath, charset);
                    fileData = new String(Files.readAllBytes(readFilePath), charset);
                    log.info("[{}]Read complete. String encoding: '{}'", txId, charset);
                    break;
                }
                case PARSED_TEXT: {
                    log.info("[{}]Reading file \"{}\" to 'FORMATTED_DATA' ...", txId, readFilePath);
                    fileData = readFormattedData(readFilePath, charset);
                    if (fileData != null) {
                        log.info("[{}]Read complete. Parsed records count: {}", txId, ((List) fileData).size());
                    } else {
                        log.info("[{}]Read complete. Parsed data is null", txId);
                    }
                    break;
                }
                default: throw new IllegalArgumentException("Unsupported output data type: " + outputDataType);
            }
        } catch (Throwable t) {
            log.warn("[{}]Failed to read file \"{}\"", txId, readFilePath);
            throw t;
        }

        if (getOutput() != null) {
            setOutputValue(ctx, fileData);
        }

    }

    private List readFormattedData(Path filePath, Charset charset) throws Exception {
        List resultData = null;
        StringBuilder content = new StringBuilder(new String(Files.readAllBytes(filePath), charset));

        String recordSeparator = this.recordSeparator;
        String delimiter = this.delimiter;
        String qualifier = this.qualifier;
        String replacementOfNullValue = this.replacementOfNullValue;
        String replacementOfEmptyValue = this.replacementOfEmptyValue;
        String replacementOfLineFeed = this.replacementOfLineFeed;
        String replacementOfCarriageReturn = this.replacementOfCarriageReturn;
        boolean headerExist = this.headerExist;
        boolean handleBinaryData = this.handleBinaryData;

        //메타데이터를 읽고 검증하는 과정이다.
        if (metadataExist) {
            int firstLineEndIdx = content.indexOf("\n");
            if (firstLineEndIdx == -1) {
                throw new IllegalStateException("Can not parse metadata of the file \"" + filePath + "\". If the 'metadataExist' property is 'true', the first line of the file must contain only metadata.");
            }
            String metadataLine = content.substring(0, firstLineEndIdx).trim();
            boolean mdPrefix = metadataLine.startsWith("<![METADATA[");
            boolean mdSuffix = metadataLine.endsWith("]]>");
            if (mdPrefix || mdSuffix)
                throw new IllegalStateException("Can not parse metadata of the file \"" + filePath + "\". The metadata is not contained in the Metadata wrapper \"<![METADATA[]>\".");
            metadataLine = metadataLine.substring("<![METADATA[".length(), metadataLine.length() - "]]>".length());

            Map<String, Object> metadata = null;
            try {
                metadata = MessageUtil.jsonToMap(metadataLine);
                throwExceptionIfMetadataInvalid(metadata);
                content.delete(0, firstLineEndIdx); //메타데이터 추출 후 데이터에서 메타데이터 부분은 지운다.
            } catch (JsonProcessingException je) {
                throw new IllegalStateException("Can not parse metadata of the file \"" + filePath + "\". The metadata is not JSON format.");
            }
            recordSeparator = (String) metadata.get("record_separator");
            delimiter = (String) metadata.get("delimiter");
            qualifier = (String) metadata.get("qualifier");
            replacementOfNullValue = (String) metadata.get("replacement_of_null_value");
            replacementOfEmptyValue = (String) metadata.get("replacement_of_empty_value");
            replacementOfLineFeed = (String) metadata.get("replacement_of_line_feed");
            replacementOfCarriageReturn = (String) metadata.get("replacement_of_carriage_return");
            headerExist = Boolean.parseBoolean((String) metadata.get("add_header"));
            handleBinaryData =  Boolean.parseBoolean((String) metadata.get("handle_binary_as_it_is"));
        }

        FileParserTemplate parserTemplate = new FileParserTemplate(recordSeparator, delimiter, qualifier,
                replacementOfNullValue, replacementOfEmptyValue, replacementOfLineFeed,
                replacementOfCarriageReturn, headerExist, handleBinaryData);

        if (headerExist) {
            //List<Map<String, Object>> 형태로 parsing
            resultData = FileParser.readDataToRecord(content.toString(), parserTemplate);
        } else {
            //List<List<Object>> 형태로 parsing
            resultData = FileParser.readDataToList(content.toString(), parserTemplate);
        }

        return resultData;
    }

    private void throwExceptionIfMetadataInvalid(Map<String, Object> metadata) {
        Assert.notNull(metadata.get("record_separator"), "Invalid metadata property 'record_separator'.");
        Assert.notNull(metadata.get("delimiter"), "Invalid metadata property 'delimiter'.");
        Assert.notNull(metadata.get("qualifier"), "Invalid metadata property 'qualifier'.");
        Assert.notNull(metadata.get("replacement_of_null_value"), "Invalid metadata property 'replacement_of_null_value'.");
        Assert.notNull(metadata.get("replacement_of_empty_value"), "Invalid metadata property 'replacement_of_empty_value'.");
        Assert.notNull(metadata.get("replacement_of_line_feed"), "Invalid metadata property 'replacement_of_line_feed'.");
        Assert.notNull(metadata.get("replacement_of_carriage_return"), "Invalid metadata property 'replacement_of_carriage_return'.");
        Assert.notNull(metadata.get("add_header"), "Invalid metadata property 'add_header'.");
        Assert.notNull(metadata.get("handle_binary_as_it_is"), "Invalid metadata property 'handle_binary_as_it_is'.");
    }

    public void setOutputDataType(DataType outputDataType) {
        switch (outputDataType) {
            case STRING: case BYTE_ARRAY: case PARSED_TEXT: this.outputDataType = outputDataType; break;
            default: throw new IllegalArgumentException("Unsupported output data type: " + outputDataType);
        }
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
}
