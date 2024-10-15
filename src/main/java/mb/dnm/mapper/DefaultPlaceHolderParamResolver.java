package mb.dnm.mapper;

import mb.dnm.code.ProcessCode;
import mb.dnm.core.context.ServiceContext;
import mb.dnm.util.TimeUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DefaultPlaceHolderParamResolver implements PlaceHolderParamResolver {
    
    /**
     * Key = 표현식<br>
     * Value = ServiceTransactionContext 의 Getter 메소드 명, ServiceTransactionContext 객체의 필드를 가져오는 것이 아닌 경우 null
     * */
    private static final Map<String, String> PARAM_MAPPER = new HashMap<>();

    static {
        PARAM_MAPPER.put("@{if_id}", "getId");
        PARAM_MAPPER.put("@{tx_id}", "getTxId");
        PARAM_MAPPER.put("@{tx_init_time}", "getInitTime");
        PARAM_MAPPER.put("@{tx_init_time_jdbc_timestamp}", "getInitTimestampForJdbc");
        PARAM_MAPPER.put("@{tx_init_time_jdbc_date}", "getInitTimeDateForJdbc");
        PARAM_MAPPER.put("@{tx_end_time}", "getEndTime");
        PARAM_MAPPER.put("@{tx_end_time_jdbc_timestamp}", "getEndTimestampForJdbc");
        PARAM_MAPPER.put("@{tx_end_time_jdbc_date}", "getEndTimeDateForJdbc");
        PARAM_MAPPER.put("@{tx_process_status}", "getProcessStatus");
        PARAM_MAPPER.put("@{tx_msg}", "getMsg");
        PARAM_MAPPER.put("@{cur_date}", null);
        PARAM_MAPPER.put("@{cur_time}", null);
        PARAM_MAPPER.put("@{cur_datetime}", null);
        PARAM_MAPPER.put("@{cur_timestamp}", null);
        PARAM_MAPPER.put("@{sysdate}", null);
        PARAM_MAPPER.put("@{systimestamp}", null);
        //PARAM_MAPPER.put("@{custom_status_msg}", null);
    }

    @Override
    public boolean isPropertyAvailable(String expression) {
        if (expression.startsWith("@{custom_status_msg}")) {
            //
            return true;
        } else {
            return PARAM_MAPPER.containsKey(expression);
        }
    }

    @Override
    public String getValueFrom(String expression, Object fromObj) throws Exception{
        if (!isPropertyAvailable(expression))
            throw new PlaceHolderParamParsingException("No such indicator '" + expression + "' in the PlaceHolderParamResolver class '"
                    + this.getClass().getName() + "'");
        if (!(fromObj instanceof ServiceContext))
            throw new PlaceHolderParamParsingException("Unsupported class type '" + fromObj.getClass().getName()
                    + "'. Current PlaceHolderParamResolver class is '" + this.getClass().getName()
                    + "'. Only supports the 'ServiceTransactionContext' class for fromObj.");
        ServiceContext context = (ServiceContext) fromObj;
        try {
            if (expression.equals("@{sysdate}")) {
                return TimeUtil.curDate(TimeUtil.JDBC_DATE_FORMAT);
            } else if (expression.equals("@{systimestamp}")) {
                return TimeUtil.curDate(TimeUtil.JDBC_TIMESTAMP_FORMAT);
            } else if (expression.equals("@{cur_date}")) {
                return TimeUtil.curDate(TimeUtil.DATE_FORMAT);
            } else if (expression.equals("@{cur_datetime}")) {
                return TimeUtil.curDate(TimeUtil.DATETIME_FORMAT);
            } else if (expression.equals("@{cur_time}")) {
                return TimeUtil.curDate(TimeUtil.HHmmss);
            } else if (expression.equals("@{cur_timestamp}")) {
                return TimeUtil.curTimeStamp();
            } else if (expression.startsWith("@{custom_status_msg}:{")) {

                ProcessCode processStatus = context.getProcessStatus();
                int delimiterIdx = expression.indexOf(":{");
                if (delimiterIdx == -1) {
                    throw new IllegalArgumentException("Invalid argument for the expression. Use like '@{custom_status_msg}:{code1=value1;code2=value2}'");
                }
                String optPhrase = expression.substring(delimiterIdx + 2, expression.length() -1);
                Map<String, String> propsMap = new HashMap<>();
                String[] options = optPhrase.split(";");
                for (String option : options) {
                    String[] props = option.split("=");
                    if (props.length == 2) {
                        propsMap.put(props[0], props[1]);
                    } else {
                        throw new IllegalArgumentException("Invalid argument for the expression. Use like '@{custom_status_msg}:{code1=value1;code2=value2}'");
                    }
                }
                String val = propsMap.get(processStatus);
                if (val == null) {
                    return processStatus.toString();
                }
                return val;
            }

            //else
            Method method = ServiceContext.class.getMethod(PARAM_MAPPER.get(expression));
            Object value = method.invoke(context);
            if (value == null)
                return null;
            return String.valueOf(value);

        } catch (Throwable t) {
           throw t;
        }
    }
}
