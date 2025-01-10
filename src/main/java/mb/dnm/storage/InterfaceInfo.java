package mb.dnm.storage;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.access.file.FileTemplate;
import mb.dnm.access.http.HttpAPITemplate;
import mb.dnm.core.context.ServiceContext;

import java.io.Serializable;
import java.util.*;

/**
 * 인터페이스에 대한 기본정보를 저장하는 객체이다.<br><br>
 *
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 * &lt;bean class="mb.dnm.storage.InterfaceInfo"&gt;
 * 	&lt;property name="activated"                  value="<span style="color: black; background-color: #FAF3D4;">사용여부</span>"/&gt;
 * 	&lt;property name="controllerInterface"        value="<span style="color: black; background-color: #FAF3D4;">컨트롤 인터페이스 여부</span>"/&gt;
 * 	&lt;property name="interfaceId"                value="<span style="color: black; background-color: #FAF3D4;">인터페이스 ID</span>"/&gt;
 * 	&lt;property name="interfaceName"              value="<span style="color: black; background-color: #FAF3D4;">인터페이스 명</span>"/&gt;
 * 	&lt;property name="description"                value="<span style="color: black; background-color: #FAF3D4;">인터페이스 설명</span>"/&gt;
 * 	&lt;property name="frontHttpUrl"               value="<span style="color: black; background-color: #FAF3D4;">HTTP 요청 수신 URL</span>"/&gt;
 * 	&lt;property name="frontHttpMethod"            value="<span style="color: black; background-color: #FAF3D4;">HTTP 요청 수신 Method</span>"/&gt;
 * 	&lt;property name="sourceCode"                 value="<span style="color: black; background-color: #FAF3D4;">인터페이스 Source 시스템 코드</span>"/&gt;
 * 	&lt;property name="targetCode"                 value="<span style="color: black; background-color: #FAF3D4;">인터페이스 Target 시스템 코드</span>"/&gt;
 * 	&lt;property name="loggingWhenNormal"          value="<span style="color: black; background-color: #FAF3D4;">정상 처리 시 Logging 여부</span>"/&gt;
 * 	&lt;property name="loggingWhenError"           value="<span style="color: black; background-color: #FAF3D4;">에러 발생 시 Logging 여부</span>"/&gt;
 * 	&lt;property name="sourceAliases"              value="<span style="color: black; background-color: #FAF3D4;">Source Alias 설정</span>"/&gt;
 * 	&lt;property name="querySequence"              value="<span style="color: black; background-color: #FAF3D4;">DB 쿼리 시퀀스 설정</span>"/&gt;
 * 	&lt;property name="errorQuerySequence"         value="<span style="color: black; background-color: #FAF3D4;">에러 발생 시 DB 쿼리 시퀀스 설정</span>"/&gt;
 * 	&lt;property name="dynamicCodeSequence"        value="<span style="color: black; background-color: #FAF3D4;">DNC(Dynamic-Code) 시퀀스 설정</span>"/&gt;
 * 	&lt;property name="errorDynamicCodeSequence"   value="<span style="color: black; background-color: #FAF3D4;">에러 발생 시 DNC(Dynamic-Code) 시퀀스 설정</span>"/&gt;
 * 	&lt;property name="serviceId"                  value="<span style="color: black; background-color: #FAF3D4;">사용할 Service-Strategy ID</span>"/&gt;
 * 	&lt;property name="errorHandlerId"             value="<span style="color: black; background-color: #FAF3D4;">사용할 Error-Handler ID</span>"/&gt;
 * 	&lt;property name="fileTemplates"&gt;
 * 	    &lt;list&gt;
 * 	        &lt;bean class="mb.dnm.access.file.FileTemplate"&gt;
 * 	            &lt;property name="templateName"        value="<span style="color: black; background-color: #FAF3D4;">파일 템플릿 명</span>"/&gt;
 *                                  .
 *                                  .
 *                                  .
 * 	        &lt;/bean&gt;
 * 	    &lt;/list&gt;
 * 	&lt;/property&gt;
 * &lt;/bean&gt;</pre>
 */
@Setter
@Getter
public class InterfaceInfo implements Serializable {
    private static final long serialVersionUID = -6626530733122262663L;
    /**
     * {@code InterfaceInfo} 사용(활성화) 여부<br>
     * default: true
     */
    protected boolean activated = true;
    /**
     * Controller {@code InterfaceInfo}로서 사용할 지 여부<br>
     * default: false
     */
    protected boolean controllerInterface = false;
    /**
     * 인터페이스 아이디<br>
     */
    protected String interfaceId;
    /**
     * 인터페이스 명<br>
     */
    protected String interfaceName;
    /**
     * 인터페이스 설명<br>
     */
    protected String description;
    /**
     * 인터페이스 Source 시스템 코드<br>
     */
    protected String sourceCode;
    /**
     * 인터페이스 Target 시스템 코드<br>
     */
    protected String targetCode;
    /**
     * {@code InterfaceInfo}가 사용하는 Service-Strategy 아이디<br>
     */
    protected String serviceId;
    /**
     * {@code InterfaceInfo}가 사용하는 Error-Handler 아이디<br>
     */
    protected String errorHandlerId;

    /**
     * The Http api template.
     */
    protected HttpAPITemplate httpAPITemplate;

    /**
     * queryId가 저장되는 일종의 Queue 역할을 하는 배열이다. 배열에 저장된 순서대로 queryId가 소진된다.
     */
    protected String[] querySequenceArr;
    /**
     * errorQueryId가 저장되는 일종의 Queue 역할을 하는 배열이다. Error-Handling 과정에서 배열에 저장된 순서대로 errorQueryId가 소진된다.
     */
    protected String[] errorQuerySequenceArr;

    protected Set<String> executorNames;
    /**
     * DB 트랜잭션을 생성할 때 트랜잭션 Timeout(second) 에 대한 설정이다.
     */
    protected int txTimeoutSecond = -1;

    /**
     * 이 {@code InterfaceInfo}가 참조하는 File/FTP 인터페이스와 관련된 설정이다.
     */
    protected Map<String, FileTemplate> fileTemplateMap;

    /**
     * 이 {@code InterfaceInfo}가 참조하는 DB/FTP/JMS Source 에 대한 Alias 가 저장된다.
     */
    protected Map<String, String> sourceAliasMap;

    /**
     * dynamicCodeId가 저장되는 일종의 Queue 역할을 하는 배열이다. 배열에 저장된 순서대로 dynamicCodeId가 소진된다.
     */
    protected String[] dynamicCodeSequenceArr;
    /**
     * errorDynamicCodeId가 저장되는 일종의 Queue 역할을 하는 배열이다. Error-Handling 과정에서 배열에 저장된 순서대로 errorDynamicCodeId가 소진된다.
     */
    protected String[] errorDynamicCodeSequenceArr;

    /**
     * 인터페이스 프로세스가 정상인 경우 Log 를 기록할 지에 대한 여부<br>
     * default: true
     */
    protected boolean loggingWhenNormal = true;
    /**
     * 인터페이스 프로세스 중 에러가 발생하는 경우 Log 를 기록할 지에 대한 여부<br>
     * default: true
     */
    protected boolean loggingWhenError = true;


    /**
     * 실행할 쿼리 Sequence 를 설정한다.<br>
     * 쿼리 Sequence는 아래처럼 문자열로 작성하며, 여러개인 경우 실행되어야 되는 쿼리의 순서대로 콤마(,)로 구분한다.<br>
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * "{@link mb.dnm.access.db.ExecutorTemplate}의 executorName" + "$" + "Mybatis Mapper 파일 namespace" + . + "Mybatis Query ID"</pre>
     *
     * "Mybatis Mapper 파일 namespace" 를 {@code InterfaceInfo}의 인터페이스 ID로 지정하는 경우 {@code @{if_id}} 알리아스를 사용해 지정할 수 있다.
     * <br>
     * 등록된 쿼리는 아래의 서비스에서 소진된다.<br>
     * {@link mb.dnm.service.db.Select}, {@link mb.dnm.service.db.Update}, {@link mb.dnm.service.db.Insert}, {@link mb.dnm.service.db.Delete}, {@link mb.dnm.service.db.CallProcedure}
     * <br><br>
     *
     * Example: {@link mb.dnm.access.db.DataSourceProvider}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.access.db.DataSourceProvider"&gt;
     *  &lt;property name="queryExecutors"&gt;
     *     &lt;list&gt;
     *     	&lt;bean class="mb.dnm.access.db.ExecutorTemplate"&gt;
     *     	    &lt;property name="templateName"       value="<span style="color: black; background-color: #FAF3D4;">SRC_DB</span>"/&gt;
     *     	    &lt;property name="dataSource"         ref="DB_1"/&gt;
     *     	    &lt;property name="configLocation"     value="mybatis-configuration.xml"/&gt;
     *     	    &lt;property name="mapperLocations"    value="classpath*:SQL_*.xml"/&gt;
     *     	&lt;/bean&gt;
     *     	&lt;bean class="mb.dnm.access.db.ExecutorTemplate"&gt;
     *     	    &lt;property name="templateName"       value="<span style="color: black; background-color: #FAF3D4;">TGT_DB</span>"/&gt;
     *     	    &lt;property name="dataSource"         ref="DB_2"/&gt;
     *     	    &lt;property name="configLocation"     value="mybatis-configuration.xml"/&gt;
     *     	    &lt;property name="mapperLocations"    value="classpath*:SQL_*.xml"/&gt;
     *     	&lt;/bean&gt;
     *     &lt;/list&gt;
     * 	&lt;/property&gt;
     * &lt;/bean&gt;
     * </pre><br>
     *
     * Example: SQL_MAPPER.xml
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
     * &lt;!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"   "http://mybatis.org/dtd/mybatis-3-mapper.dtd"&gt;
     * &lt;mapper namespace="<span style="color: black; background-color: #FAF3D4;">IF_TEST</span>"&gt;
     *
     *    &lt;select id="<span style="color: black; background-color: #FAF3D4;">SELECT</span>" resultType="java.util.HashMap"&gt;
     *        SELECT * FROM A_TABLE_OF_DB_1 WHERE TO_SEND = 'Y'
     *    &lt;/select&gt;
     *
     *    &lt;insert  id="<span style="color: black; background-color: #FAF3D4;">INSERT</span>"&gt;
     *        INSERT INTO B_TABLE_OF_DB_2 (...) VALUES (...)
     *    &lt;/insert&gt;
     *
     *    &lt;update  id="<span style="color: black; background-color: #FAF3D4;">UPDATE</span>"&gt;
     *        UPDATE A_TABLE_OF_DB_1 SET TO_SEND = 'N' WHERE Unique_Column_Name = 'Unique Value'
     *    &lt;/update&gt;
     *
     * &lt;/mapper&gt;</pre><br>
     *
     * Example: {@link InterfaceInfo}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;bean class="mb.dnm.storage.InterfaceInfo""&gt;
     *    &lt;property name="interfaceId"            value="IF_TEST"/&gt;
     *                 .
     *                 .
     *                 .
     *    &lt;property name="sourceCode"             value="SRC"/&gt;
     *    &lt;property name="targetCode"             value="TGT"/&gt;
     *    <span style="color: black; background-color: #FAF3D4;">&lt;property name="querySequence"          value="SRC_DB@{if_id}.SELECT, TGT_DB@{if_id}.INSERT, SRC_DB@{if_id}.UPDATE"/&gt;</span>
     *    &lt;property name="serviceId"              value="SELECT_INSERT_UPDATE"/&gt;
     *&lt;/bean&gt;
     *
     * >SRC_DB@{if_id}.SELECT : namespace 가 IF_TEST 이고 id가 SELECT 인 쿼리가 SRC_DB(DB_1)에서 실행됨.
     * >TGT_DB@{if_id}.INSERT : namespace 가 IF_TEST 이고 id가 INSERT 인 쿼리가 TGT_DB(DB_2)에서 실행됨.
     * >SRC_DB@{if_id}.UPDATE : namespace 가 IF_TEST 이고 id가 UPDATE 인 쿼리가 SRC_DB(DB_1)에서 실행됨.
     * </pre>
     *
     * @param querySequence the query sequence
     * @see mb.dnm.service.db.Select
     * @see mb.dnm.service.db.Update
     * @see mb.dnm.service.db.Insert
     * @see mb.dnm.service.db.Delete
     * @see mb.dnm.service.db.CallProcedure
     * @see ServiceContext#hasMoreQueryMaps()
     * @see ServiceContext#nextQueryMap()
     * @see ServiceContext#setCurrentQueryOrder(int)
     * @see ServiceContext#getCurrentQueryOrder()
     */
    public void setQuerySequence(String querySequence) {
        this.querySequenceArr = parseQuerySequence(querySequence);
    }

    /**
     * {@link mb.dnm.core.ServiceProcessor} 의 Error-Handling 과정에서 사용되는 errorQuerySequence를 지정한다.<br><br>
     *
     * errorQuerySequence에 등록된 쿼리는 Service-Chaining의 Error-Handling 과정 중의 아래의 서비스에서 소진된다.<br>
     * {@link mb.dnm.service.db.Select}, {@link mb.dnm.service.db.Update}, {@link mb.dnm.service.db.Insert}, {@link mb.dnm.service.db.Delete}, {@link mb.dnm.service.db.CallProcedure}
     * <br><br>
     * Example: SQL_MAPPER.xml
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
     * &lt;!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"   "http://mybatis.org/dtd/mybatis-3-mapper.dtd"&gt;
     * &lt;mapper namespace="<span style="color: black; background-color: #FAF3D4;">IF_TEST</span>"&gt;
     *
     *    &lt;select id="<span style="color: black; background-color: #FAF3D4;">SELECT</span>" resultType="java.util.HashMap"&gt;
     *        SELECT * FROM A_TABLE_OF_DB_1 WHERE TO_SEND = 'Y'
     *    &lt;/select&gt;
     *
     *    &lt;insert  id="<span style="color: black; background-color: #FAF3D4;">INSERT</span>"&gt;
     *        INSERT INTO B_TABLE_OF_DB_2 (...) VALUES (...)
     *    &lt;/insert&gt;
     *
     *    &lt;update  id="<span style="color: black; background-color: #FAF3D4;">UPDATE</span>"&gt;
     *        UPDATE A_TABLE_OF_DB_1 SET TO_SEND = 'N' WHERE Unique_Column_Name = #{Unique_Column_Name}
     *    &lt;/update&gt;
     *
     *    &lt;update  id="<span style="color: black; background-color: #FAF3D4;">UPDATE_WHEN_ERROR</span>"&gt;
     *        UPDATE A_TABLE_OF_DB_1 SET TO_SEND = 'F' WHERE Unique_Column_Name = #{Unique_Column_Name}
     *    &lt;/update&gt;
     *
     * &lt;/mapper&gt;</pre><br>
     *
     * Example: {@link InterfaceInfo}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;bean class="mb.dnm.storage.InterfaceInfo""&gt;
     *    &lt;property name="interfaceId"            value="IF_TEST"/&gt;
     *                 .
     *                 .
     *                 .
     *    &lt;property name="sourceCode"             value="SRC"/&gt;
     *    &lt;property name="targetCode"             value="TGT"/&gt;
     *    &lt;property name="querySequence"          value="SRC_DB@{if_id}.SELECT, TGT_DB@{if_id}.INSERT, SRC_DB@{if_id}.UPDATE"/&gt;
     *    <span style="color: black; background-color: #FAF3D4;">&lt;property name="errorQuerySequence"     value="SRC_DB@{if_id}.UPDATE_WHEN_ERROR"/&gt;</span>
     *    &lt;property name="serviceId"              value="SELECT_INSERT_UPDATE"/&gt;
     *    &lt;property name="errorHandlerId"         value="ERROR_UPDATE"/&gt;
     *&lt;/bean&gt;</pre><br><br>
     *
     * >SRC_DB@{if_id}.SELECT : namespace 가 IF_TEST 이고 id가 ERROR_UPDATE 인 쿼리가 SRC_DB(DB_1)에서 실행됨.
     * @param errorQuerySequence the error query sequence
     * @see mb.dnm.service.db.Select
     * @see mb.dnm.service.db.Update
     * @see mb.dnm.service.db.Insert
     * @see mb.dnm.service.db.Delete
     * @see mb.dnm.service.db.CallProcedure
     * @see ServiceContext#hasMoreErrorQueryMaps()
     * @see ServiceContext#nextErrorQueryMap()
     * @see ServiceContext#setCurrentErrorQueryOrder(int)
     * @see ServiceContext#getCurrentErrorQueryOrder()
     */
    public void setErrorQuerySequence(String errorQuerySequence) {
        this.errorQuerySequenceArr = parseQuerySequence(errorQuerySequence);
    }

    /**
     * DNC(DynamicCode) 시퀀스를 등록한다.<br>
     * DynamicCode sequence는 아래처럼 문자열로 작성하며, 여러개인 경우 실행되어야 되는 DNC의 순서대로 콤마(,)로 구분한다.<br>
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * "#namespace" + "#code_id"</pre>
     * "#namespace" 를 {@code InterfaceInfo}의 인터페이스 ID로 지정하는 경우 {@code @{if_id}} 알리아스를 사용해 지정할 수 있다.<br>
     * 등록된 DNC는 {@link mb.dnm.service.dynamic.ExecuteDynamicCode}에서 소진된다.
     * <br><br>
     *
     * Example: {@link mb.dnm.access.dynamic.DynamicCodeProvider}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.access.dynamic.DynamicCodeProvider"&gt;
     *     &lt;property name="codeLocations"      value="classpath*:*.dnc"/&gt;
     * &lt;/bean&gt;</pre><br>
     *
     * Example : DNC_COMMON.dnc
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * #namespace: <span style="color: black; background-color: #FAF3D4;">COMMON</span>
     * #code_id : <span style="color: black; background-color: #FAF3D4;">LOGGING</span>
     * #{
     *    //Logging data
     * }#</pre><br>
     *
     * Example : DNC_IF_TEST.dnc
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * #namespace: <span style="color: black; background-color: #FAF3D4;">IF_TEST</span>
     * #code_id : <span style="color: black; background-color: #FAF3D4;">MAP_DATA</span>
     * #{
     *    //Mapping data
     * }#
     * </pre><br>
     *
     * Example: {@link InterfaceInfo}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;bean class="mb.dnm.storage.InterfaceInfo""&gt;
     *    &lt;property name="interfaceId"            value="IF_TEST"/&gt;
     *                 .
     *                 .
     *                 .
     *    &lt;property name="sourceCode"             value="SRC"/&gt;
     *    &lt;property name="targetCode"             value="TGT"/&gt;
     *    <span style="color: black; background-color: #FAF3D4;">&lt;property name="dynamicCodeSequence"    value="@{if_id}.MAP_DATA, COMMON.LOGGING"/&gt;</span>
     *    &lt;property name="serviceId"              value="EXECUTE_DNC"/&gt;
     *&lt;/bean&gt;
     *
     * </pre>
     * @param dynamicCodeSequence the dynamic code sequence
     * @see mb.dnm.service.dynamic.ExecuteDynamicCode
     * @see ServiceContext#hasMoreDynamicCodes()
     * @see ServiceContext#nextDynamicCodeId()
     * @see ServiceContext#setCurrentDynamicCodeOrder(int)
     * @see ServiceContext#getCurrentDynamicCodeOrder()
     */
    public void setDynamicCodeSequence(String dynamicCodeSequence) {
        this.dynamicCodeSequenceArr = parseDynamicCodeSequence(dynamicCodeSequence);
    }

    /**
     * DNC(DynamicCode) 시퀀스를 등록한다.<br>
     * DynamicCode sequence는 아래처럼 문자열로 작성하며, 여러개인 경우 실행되어야 되는 DNC의 순서대로 콤마(,)로 구분한다.<br>
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * "#namespace" + "#code_id"</pre>
     * "#namespace" 를 {@code InterfaceInfo}의 인터페이스 ID로 지정하는 경우 {@code @{if_id}} 알리아스를 사용해 지정할 수 있다.<br>
     * 등록된 DNC는 {@link mb.dnm.service.dynamic.ExecuteDynamicCode}에서 소진된다.
     * <br><br>
     *
     * Example: {@link mb.dnm.access.dynamic.DynamicCodeProvider}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.access.dynamic.DynamicCodeProvider"&gt;
     *     &lt;property name="codeLocations"      value="classpath*:*.dnc"/&gt;
     * &lt;/bean&gt;</pre><br>
     *
     * Example : DNC_COMMON.dnc
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * #namespace: <span style="color: black; background-color: #FAF3D4;">COMMON</span>
     * #code_id : <span style="color: black; background-color: #FAF3D4;">LOGGING</span>
     * #{
     *    //Logging data
     * }#</pre><br>
     *
     * #code_id : <span style="color: black; background-color: #FAF3D4;">HANDLE_ERROR</span>
     * #{
     *    //Handle error
     * }#</pre><br>
     *
     * Example : DNC_IF_TEST.dnc
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * #namespace: <span style="color: black; background-color: #FAF3D4;">IF_TEST</span>
     * #code_id : <span style="color: black; background-color: #FAF3D4;">MAP_DATA</span>
     * #{
     *    //Mapping data
     * }#
     * </pre><br>
     *
     * Example: {@link InterfaceInfo}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *&lt;bean class="mb.dnm.storage.InterfaceInfo""&gt;
     *    &lt;property name="interfaceId"            value="IF_TEST"/&gt;
     *                 .
     *                 .
     *                 .
     *    &lt;property name="sourceCode"                  value="SRC"/&gt;
     *    &lt;property name="targetCode"                  value="TGT"/&gt;
     *    &lt;property name="dynamicCodeSequence"         value="@{if_id}.MAP_DATA, COMMON.LOGGING"/&gt;
     *    <span style="color: black; background-color: #FAF3D4;">&lt;property name="errorDynamicCodeSequence"    value="COMMON.HANDLE_ERROR"/&gt;</span>
     *    &lt;property name="serviceId"                   value="EXECUTE_DNC"/&gt;
     *&lt;/bean&gt;</pre>
     *
     * @param errorDynamicCodeSequence the error dynamic code sequence
     * @see mb.dnm.service.dynamic.ExecuteDynamicCode
     * @see ServiceContext#hasMoreErrorDynamicCodes()
     * @see ServiceContext#nextErrorDynamicCodeId()
     * @see ServiceContext#setCurrentErrorDynamicCodeOrder(int)
     * @see ServiceContext#getCurrentErrorDynamicCodeOrder()
     */
    public void setErrorDynamicCodeSequence(String errorDynamicCodeSequence) {
        this.errorDynamicCodeSequenceArr = parseDynamicCodeSequence(errorDynamicCodeSequence);
    }

    /**
     * {@code dynamicCodeSequence}를 가져온다.
     *
     * @return the string [ ]
     */
    public String[] getDynamicCodeSequence() {
        return dynamicCodeSequenceArr;
    }

    /**
     * {@code errorDynamicCodeSequence}를 가져온다.
     *
     * @return the string [ ]
     */
    public String[] getErrorDynamicCodeSequence() {
        return errorDynamicCodeSequenceArr;
    }

    private String[] parseQuerySequence(String querySequence) {
        if (interfaceId == null)
            throw new IllegalStateException("Please register interfaceId first.");

        querySequence = querySequence.trim();
        if (querySequence.isEmpty()) {
            throw new IllegalArgumentException("querySequence is empty");
        }

        String[] tempQueries = querySequence.split(",");
        String[] queries = new String[tempQueries.length];

        int i = 0;
        for (String query : tempQueries) {
            query = query.trim();
            if (query.isEmpty()) {
                throw new IllegalArgumentException("query is empty");
            }
            //DataSource separating character '$' index
            int dsSepIdx = query.indexOf('$');
            if (dsSepIdx == -1) {
                throw new IllegalArgumentException("The query must contain the QueryExecutor separating character '$': " + query);
            }

            if (dsSepIdx != query.lastIndexOf('$')) {
                throw new IllegalArgumentException("The query has the duplicated QueryExecutor separating character '$': " + query);
            }
            String executorName = query.substring(0, dsSepIdx).trim();
            String queryId = query.substring(dsSepIdx + 1).trim();

            if (executorName.isEmpty())
                throw new IllegalArgumentException("The QueryExecutor name is empty: " + query);
            if (executorNames == null) {
                executorNames = new LinkedHashSet<>();
            }
            executorNames.add(executorName);

            if (queryId.isEmpty())
                throw new IllegalArgumentException("The query id is empty: " + query);


            queries[i] = query.replace("@{if_id}", interfaceId);
            ++i;
        }
        return queries;
    }

    private String[] parseDynamicCodeSequence(String dynamicCodeSequence) {
        if (interfaceId == null)
            throw new IllegalStateException("Please register interfaceId first.");

        dynamicCodeSequence = dynamicCodeSequence.trim();
        if (dynamicCodeSequence.isEmpty()) {
            throw new IllegalArgumentException("dynamicCodeSequence is empty");
        }

        String[] tempCodeId = dynamicCodeSequence.split(",");
        String[] codes = new String[tempCodeId.length];

        int i = 0;
        for (String code : tempCodeId) {
            code = code.trim();
            if (code.isEmpty()) {
                throw new IllegalArgumentException("code id is empty");
            }
            codes[i] = code.replace("@{if_id}", interfaceId);
            ++i;
        }
        return codes;
    }

    /**
     * {@code InterfaceInfo}가 사용하는 DB/FTP/JMS 등의 source 에 접근할 때 사용할 알리아스를 지정한다.
     *
     *
     * @param aliasExpression the alias expression
     */
    public void setSourceAliases(String aliasExpression) {
        aliasExpression = aliasExpression.trim();
        if (aliasExpression.isEmpty()) {
            throw new IllegalArgumentException("The aliasExpression is empty");
        }
        String[] aliasExpressions = aliasExpression.split(",");

        if (sourceAliasMap == null) {
            sourceAliasMap = new HashMap<>();
        }

        for (String expression : aliasExpressions) {
            expression = expression.trim();
            if (expression.isEmpty())
                continue;
            String[] aliasAndSource = expression.split(":");
            if (aliasAndSource.length != 2) {
                throw new IllegalArgumentException("The expression must contain the alias and source expression[alias:source]: " + expression);
            }
            String alias = aliasAndSource[0].trim();
            String source = aliasAndSource[1].trim();
            if (alias.isEmpty())
                throw new IllegalArgumentException("The alias part of sourceAlias expression is empty: " + expression);
            if (source.isEmpty())
                throw new IllegalArgumentException("The source part sourceAlias expression is empty: " + expression);

            if (sourceAliasMap.containsKey(alias))
                throw new IllegalStateException("The alias '" + alias + "' already exists: " + expression);

            sourceAliasMap.put(alias, source);
        }

    }

    /**
     * Gets source name by alias.
     *
     * @param alias the alias
     * @return the source name by alias
     */
    public String getSourceNameByAlias(String alias) {
        if (sourceAliasMap == null) {
            return null;
        }
        return sourceAliasMap.get(alias);
    }

    /**
     * Sets file templates.
     *
     * @param fileTemplates the file templates
     */
    public void setFileTemplates(List<FileTemplate> fileTemplates) {
        String templateName = null;
        if (fileTemplateMap == null) {
            fileTemplateMap = new HashMap<>();
        }
        for (FileTemplate fileTemplate : fileTemplates) {
            templateName = fileTemplate.getTemplateName();
            if (templateName == null || templateName.trim().isEmpty())
                throw new IllegalArgumentException("The file template name is null");
            fileTemplateMap.put(templateName, fileTemplate);
        }
    }

    /**
     * Gets file template.
     *
     * @param templateName the template name
     * @return the file template
     */
    public FileTemplate getFileTemplate(String templateName) {
        try {
            return fileTemplateMap.get(templateName);
        } catch (NullPointerException ne) {
            throw new NullPointerException("The file template map is not initialized.");
        }
    }

    /**
     * Get query sequence string [ ].
     *
     * @return the string [ ]
     */
    public String[] getQuerySequence() {
        return querySequenceArr;
    }

    /**
     * Get error query sequence string [ ].
     *
     * @return the string [ ]
     */
    public String[] getErrorQuerySequence() {
        return errorQuerySequenceArr;
    }

    /**
     * Sets front http method.
     *
     * @param methods the methods
     */
    public void setFrontHttpMethod(String methods) {
        if (httpAPITemplate == null) {
            httpAPITemplate = new HttpAPITemplate();
        }
        httpAPITemplate.setFrontMethods(methods);
    }

    /**
     * Is permitted http method boolean.
     *
     * @param httpMethod the http method
     * @return the boolean
     */
    public boolean isPermittedHttpMethod(String httpMethod) {
        if (httpAPITemplate == null) {
            return false;
        }
        return httpAPITemplate.isPermittedFrontMethod(httpMethod);
    }

    /**
     * Sets front http url.
     *
     * @param httpUrl the http url
     */
    public void setFrontHttpUrl(String httpUrl) {
        if (httpAPITemplate == null) {
            httpAPITemplate = new HttpAPITemplate();
        }
        httpAPITemplate.setFrontUrl(httpUrl);
    }

    /**
     * Gets front http url.
     *
     * @return the front http url
     */
    public String getFrontHttpUrl() {
        if (httpAPITemplate == null) {
            return null;
        }
        return httpAPITemplate.getFrontUrl();
    }

}
