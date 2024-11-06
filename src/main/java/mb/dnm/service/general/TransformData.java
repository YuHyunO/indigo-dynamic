package mb.dnm.service.general;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mb.dnm.code.DataType;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.dispatcher.http.HttpRequestDispatcher;
import mb.dnm.exeption.InvalidServiceConfigurationException;
import mb.dnm.service.ParameterAssignableService;
import mb.dnm.util.MessageUtil;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;


/**
 * Input으로 전달된 데이터의 타입을 지정된 타입으로 변환한다.<br>
 * 
 * 변환 가능한 input 과 output DataType set은 다음과 같다.<br>
 * <code>DataType.BYTE_ARRAY</code><br>
 * <code>DataType.STRING</code><br>
 * <code>DataType.JSON</code><br>
 * <code>DataType.XML</code><br>
 * <code>DataType.MAP</code><br>
 *
 * @see DataType
 *
 * @author Yuhyun O
 * @version 2024.09.27
 * 
 * @Input input으로 전달 받을 parameter의 이름
 * @InputType <code>inputDataType</code> 속성에 지정된 DataType
 * @Output 타입을 변환한 데이터를 다음 서비스로 전달할 output 파라미터 명
 * @OutputType <code>outputDataType</code> 속성에 지정된 DataType
 *
 * @Throws 
 * <code>InvalidServiceConfigurationException</code>: <code>input</code>, <code>output</code>, <code>inputDataType</code>, <code>outputDataType</code> 을 모두 지정하지 않은 경우
 * <code>IllegalStateException</code>: input 과 output 간의 타입변환이 지원되지 않는 경우
 * */
@Slf4j
@Setter
@Getter
public class TransformData extends ParameterAssignableService implements Serializable {
    private static final long serialVersionUID = 6118071336424114828L;
    private DataType inputDataType;
    private DataType outputDataType;
    /**
     * 기본값: UTF-8<br>
     * 문자데이터 중 하나로 변환되는 경우 설정된 charset으로 인코딩 된다.
     * */
    private Charset charset = Charset.forName("UTF-8");
    /**
     * 기본값: true<br>
     * 데이터 변환 후 기존 데이터를 <code>ServiceContext</code>에서 삭제할 지에 대한 설정이다.
     * */
    private boolean removeOriginalData = true;

    @Override
    public void process(ServiceContext ctx) throws Throwable {
        if (getInput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "No input data is assigned");
        }
        if (getOutput() == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "No output data is assigned");
        }
        if (inputDataType == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "No input data type is assigned");
        }
        if (outputDataType == null) {
            throw new InvalidServiceConfigurationException(this.getClass(), "No output data type is assigned");
        }

        Object inputVal = getInputValue(ctx);
        if (inputVal == null) {
            log.info("[{}]The http body input value is null", ctx.getTxId());
            return;
        }

        if (inputDataType == DataType.BYTE_ARRAY && outputDataType == DataType.STRING) {
        //BYTE_ARRAY -> STRING
            if (inputVal instanceof String) {
              setOutputValue(ctx, new String(inputVal.toString().getBytes(charset), charset));

            } if (!(inputVal instanceof byte[])) {
                throw new IllegalStateException("The property 'inputDataType' is BYTE_ARRAY. But the value is not a byte array");

            } else {
                setOutputValue(ctx, new String((byte[]) inputVal, charset));

            }

        } else if (inputDataType == DataType.STRING && outputDataType == DataType.BYTE_ARRAY) {
        //STRING -> BYTE_ARRAY
            if (inputVal instanceof byte[]) {
                setOutputValue(ctx, inputVal);

            } else if (!(inputVal instanceof String)) {
                throw new IllegalStateException("The property 'inputDataType' is STRING. But the value is not a string");

            } else {
                setOutputValue(ctx,inputVal.toString().getBytes(charset));

            }

        } else if (inputDataType == DataType.JSON && outputDataType == DataType.MAP) {
        //JSON -> MAP
            if (inputVal instanceof Map) {
                setOutputValue(ctx, inputVal);

            } else if (inputVal instanceof byte[]) {
                try {
                    log.debug("[{}]The detected input value type is byte array. Trying to convert this byte[] to JSON String and finally a Map", ctx.getTxId());
                    setOutputValue(ctx, MessageUtil.jsonToMap(new String((byte[])inputVal, charset)));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Can not convert input value to MAP. The input value is not a json format", e);
                }

            } else if (!(inputVal instanceof String)) {
                throw new IllegalStateException("The property 'inputDataType' is JSON. But the value is not a json string");

            } else {
                try {
                    setOutputValue(ctx, MessageUtil.jsonToMap(inputVal.toString()));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Can not convert input value to Map. The input value is invalid json format", e);
                }

            }

        } else if (inputDataType == DataType.MAP && outputDataType == DataType.XML) {
        //MAP -> XML
            if (!(inputVal instanceof Map)) {
                throw new IllegalStateException("The property 'inputDataType' is MAP. But the value is not a xml string");

            } else {
                try {
                    setOutputValue(ctx, MessageUtil.mapToXml((Map) inputVal, false));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Can not convert input value to XML.", e);
                }

            }

        } else if (inputDataType == DataType.MAP && outputDataType == DataType.JSON) {
        //MAP -> JSON

            if (inputVal instanceof String) {
                try {
                    MessageUtil.jsonToMap(inputVal.toString());
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Can not convert input value to JSON. The input value is already a string type. But it is not json format", e);
                }
            } else if (!(inputVal instanceof Map)) {
                throw new IllegalStateException("The property 'inputDataType' is MAP. But the value is not a Map");

            } else {
                try {
                    setOutputValue(ctx, MessageUtil.mapToJson((Map)inputVal, false));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Can not convert input value to JSON.", e);
                }
            }

        } else if (inputDataType == DataType.BYTE_ARRAY && outputDataType == DataType.MAP) {
        //BYTE_ARRAY
            if (inputVal instanceof Map) {
                setOutputValue(ctx, inputVal);

            } else if (inputVal instanceof String) {
                try {
                    setOutputValue(ctx, MessageUtil.jsonToMap(inputVal.toString()));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Can not convert input value to MAP. The input value is a string type. But it is not json format", e);
                }

            } else if (!(inputVal instanceof byte[])) {
                throw new IllegalStateException("The property 'inputDataType' is BYTE_ARRAY. But the value is not a byte array");

            } else {
                try {
                    log.debug("[{}]The detected input value type is byte array. Trying to convert this byte[] to JSON String and finally a Map", ctx.getTxId());
                    setOutputValue(ctx, MessageUtil.jsonToMap(new String((byte[])inputVal, charset)));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Can not convert input value to MAP. The input value is not a json format", e);
                }

            }
        } else {
            throw new IllegalStateException("Not supported case of data transform. " + inputDataType + " -> " + outputDataType);
        }

    }

    public void setCharset(String charset) {
        this.charset = Charset.forName(charset);
    }

}
