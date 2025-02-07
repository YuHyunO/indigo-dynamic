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

    /**
     * The Executor names.
     */
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
     * <p>
     * "Mybatis Mapper 파일 namespace" 를 {@code InterfaceInfo}의 인터페이스 ID로 지정하는 경우 {@code @{if_id}} 알리아스를 사용해 지정할 수 있다.
     * <br>
     * 등록된 쿼리는 아래의 서비스에서 소진된다.<br>
     * {@link mb.dnm.service.db.Select}, {@link mb.dnm.service.db.Update}, {@link mb.dnm.service.db.Insert}, {@link mb.dnm.service.db.Delete}, {@link mb.dnm.service.db.CallProcedure}
     * <br><br>
     * <p>
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
     * <p>
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
     * <p>
     * Example: {@link InterfaceInfo}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.storage.InterfaceInfo""&gt;
     *    &lt;property name="interfaceId"            value="IF_TEST"/&gt;
     *                 .
     *                 .
     *                 .
     *    &lt;property name="sourceCode"             value="SRC"/&gt;
     *    &lt;property name="targetCode"             value="TGT"/&gt;
     *    <span style="color: black; background-color: #FAF3D4;">&lt;property name="querySequence"          value="SRC_DB@{if_id}.SELECT, TGT_DB@{if_id}.INSERT, SRC_DB@{if_id}.UPDATE"/&gt;</span>
     *    &lt;property name="serviceId"              value="SELECT_INSERT_UPDATE"/&gt;
     * &lt;/bean&gt;
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
     * @see ServiceContext#hasMoreQueryMaps() ServiceContext#hasMoreQueryMaps()
     * @see ServiceContext#nextQueryMap() ServiceContext#nextQueryMap()
     * @see ServiceContext#setCurrentQueryOrder(int) ServiceContext#setCurrentQueryOrder(int)
     * @see ServiceContext#getCurrentQueryOrder() ServiceContext#getCurrentQueryOrder()
     */
    public void setQuerySequence(String querySequence) {
        this.querySequenceArr = parseQuerySequence(querySequence);
    }

    /**
     * {@link mb.dnm.core.ServiceProcessor} 의 Error-Handling 과정에서 사용되는 errorQuerySequence를 지정한다.<br><br>
     * <p>
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
     * <p>
     * Example: {@link InterfaceInfo}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.storage.InterfaceInfo""&gt;
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
     * &lt;/bean&gt;</pre><br><br>
     * <p>
     * >SRC_DB@{if_id}.SELECT : namespace 가 IF_TEST 이고 id가 ERROR_UPDATE 인 쿼리가 SRC_DB(DB_1)에서 실행됨.
     *
     * @param errorQuerySequence the error query sequence
     * @see mb.dnm.service.db.Select
     * @see mb.dnm.service.db.Update
     * @see mb.dnm.service.db.Insert
     * @see mb.dnm.service.db.Delete
     * @see mb.dnm.service.db.CallProcedure
     * @see ServiceContext#hasMoreErrorQueryMaps() ServiceContext#hasMoreErrorQueryMaps()
     * @see ServiceContext#nextErrorQueryMap() ServiceContext#nextErrorQueryMap()
     * @see ServiceContext#setCurrentErrorQueryOrder(int) ServiceContext#setCurrentErrorQueryOrder(int)
     * @see ServiceContext#getCurrentErrorQueryOrder() ServiceContext#getCurrentErrorQueryOrder()
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
     * <p>
     * Example: {@link mb.dnm.access.dynamic.DynamicCodeProvider}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.access.dynamic.DynamicCodeProvider"&gt;
     *     &lt;property name="codeLocations"      value="classpath*:*.dnc"/&gt;
     * &lt;/bean&gt;</pre><br>
     * <p>
     * Example : DNC_COMMON.dnc
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * #namespace: <span style="color: black; background-color: #FAF3D4;">COMMON</span>
     * #code_id : <span style="color: black; background-color: #FAF3D4;">LOGGING</span>
     * #{
     *    //Logging data
     * }#</pre><br>
     * <p>
     * Example : DNC_IF_TEST.dnc
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * #namespace: <span style="color: black; background-color: #FAF3D4;">IF_TEST</span>
     * #code_id : <span style="color: black; background-color: #FAF3D4;">MAP_DATA</span>
     * #{
     *    //Mapping data
     * }#
     * </pre><br>
     * <p>
     * Example: {@link InterfaceInfo}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.storage.InterfaceInfo""&gt;
     *    &lt;property name="interfaceId"            value="IF_TEST"/&gt;
     *                 .
     *                 .
     *                 .
     *    &lt;property name="sourceCode"             value="SRC"/&gt;
     *    &lt;property name="targetCode"             value="TGT"/&gt;
     *    <span style="color: black; background-color: #FAF3D4;">&lt;property name="dynamicCodeSequence"    value="@{if_id}.MAP_DATA, COMMON.LOGGING"/&gt;</span>
     *    &lt;property name="serviceId"              value="EXECUTE_DNC"/&gt;
     * &lt;/bean&gt;
     *
     * </pre>
     *
     * @param dynamicCodeSequence the dynamic code sequence
     * @see mb.dnm.service.dynamic.ExecuteDynamicCode
     * @see ServiceContext#hasMoreDynamicCodes() ServiceContext#hasMoreDynamicCodes()
     * @see ServiceContext#nextDynamicCodeId() ServiceContext#nextDynamicCodeId()
     * @see ServiceContext#setCurrentDynamicCodeOrder(int) ServiceContext#setCurrentDynamicCodeOrder(int)
     * @see ServiceContext#getCurrentDynamicCodeOrder() ServiceContext#getCurrentDynamicCodeOrder()
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
     * <p>
     * Example: {@link mb.dnm.access.dynamic.DynamicCodeProvider}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.access.dynamic.DynamicCodeProvider"&gt;
     *     &lt;property name="codeLocations"      value="classpath*:*.dnc"/&gt;
     * &lt;/bean&gt;</pre><br>
     * <p>
     * Example : DNC_COMMON.dnc
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * #namespace: <span style="color: black; background-color: #FAF3D4;">COMMON</span>
     * #code_id : <span style="color: black; background-color: #FAF3D4;">LOGGING</span>
     * #{
     *    //Logging data
     * }#</pre><br>
     * <p>
     * #code_id : <span style="color: black; background-color: #FAF3D4;">HANDLE_ERROR</span>
     * #{
     * //Handle error
     * }#</pre><br>
     * <p>
     * Example : DNC_IF_TEST.dnc
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * #namespace: <span style="color: black; background-color: #FAF3D4;">IF_TEST</span>
     * #code_id : <span style="color: black; background-color: #FAF3D4;">MAP_DATA</span>
     * #{
     * //Mapping data
     * }#
     * </pre><br>
     * <p>
     * Example: {@link InterfaceInfo}
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.storage.InterfaceInfo""&gt;
     * &lt;property name="interfaceId"            value="IF_TEST"/&gt;
     * .
     * .
     * .
     * &lt;property name="sourceCode"                  value="SRC"/&gt;
     * &lt;property name="targetCode"                  value="TGT"/&gt;
     * &lt;property name="dynamicCodeSequence"         value="@{if_id}.MAP_DATA, COMMON.LOGGING"/&gt;
     * <span style="color: black; background-color: #FAF3D4;">&lt;property name="errorDynamicCodeSequence"    value="COMMON.HANDLE_ERROR"/&gt;</span>
     * &lt;property name="serviceId"                   value="EXECUTE_DNC"/&gt;
     * &lt;/bean&gt;</pre>
     *
     * @param errorDynamicCodeSequence the error dynamic code sequence
     * @see mb.dnm.service.dynamic.ExecuteDynamicCode
     * @see ServiceContext#hasMoreErrorDynamicCodes() ServiceContext#hasMoreErrorDynamicCodes()
     * @see ServiceContext#nextErrorDynamicCodeId() ServiceContext#nextErrorDynamicCodeId()
     * @see ServiceContext#setCurrentErrorDynamicCodeOrder(int) ServiceContext#setCurrentErrorDynamicCodeOrder(int)
     * @see ServiceContext#getCurrentErrorDynamicCodeOrder() ServiceContext#getCurrentErrorDynamicCodeOrder()
     */
    public void setErrorDynamicCodeSequence(String errorDynamicCodeSequence) {
        this.errorDynamicCodeSequenceArr = parseDynamicCodeSequence(errorDynamicCodeSequence);
    }

    /**
     * {@code dynamicCodeSequence}를 가져온다.
     *
     * @return the dynamicCode sequence queue.
     */
    public String[] getDynamicCodeSequence() {
        return dynamicCodeSequenceArr;
    }

    /**
     * {@code errorDynamicCodeSequence}를 가져온다.
     *
     * @return the errorDynamicCode sequence queue.
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
     * {@code InterfaceInfo}가 사용하는 FTP/JMS 등의 source 에 접근할 때 사용할 알리아스를 지정한다.
     * <br>
     * DB/FTP/JMS 와 같은 source 는 {@link mb.dnm.access.ftp.FTPSourceProvider}의 {@code ftpClients}, {@link mb.dnm.access.jms.JMSSourceProvider}의 {@code jmsTemplates} 등을 의미한다.
     * <br>
     * <br>
     * Example : {@link mb.dnm.access.ftp.FTPSourceProvider}, {@link mb.dnm.access.ftp.FTPClientTemplate}
     * <br>아래의 {@code FTPSourceProvider} 에는 2개의 FTP 서버접속 설정이 등록되어 있다.
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.access.ftp.FTPSourceProvider"&gt;
     *     &lt;property name="ftpClients"&gt;
     *         &lt;list&gt;
     *             &lt;bean class="mb.dnm.access.ftp.FTPClientTemplate"&gt;
     *                 &lt;property name="templateName"               value="<span style="color: black; background-color: #FAF3D4;">ELD_FTP_SERVER</span>"/&gt;
     *                 &lt;property name="host"                       value="10.2.456.789"/&gt;
     *                 &lt;property name="port"                       value="20"/&gt;
     *                 &lt;property name="user"                       value="ELD_USER"/&gt;
     *                 &lt;property name="password"                   value="12345"/&gt;
     *                 &lt;property name="controlEncoding"            value="MS949"/&gt;
     *                 &lt;property name="serverLanguageCode"         value="ko_KR"/&gt;
     *                 &lt;property name="serverKey"                  value="WINDOWS"/&gt;
     *                 &lt;property name="debugCommandAndReply"       value="false"/&gt;
     *             &lt;/bean&gt;
     *             &lt;bean class="mb.dnm.access.ftp.FTPClientTemplate"&gt;
     *                 &lt;property name="templateName"               value="<span style="color: black; background-color: #FAF3D4;">HEZ_FTP_SERVER</span>"/&gt;
     *                 &lt;property name="host"                       value="12.2.345.678"/&gt;
     *                 &lt;property name="port"                       value="20"/&gt;
     *                 &lt;property name="user"                       value="HEZ_USER"/&gt;
     *                 &lt;property name="password"                   value="12345"/&gt;
     *                 &lt;property name="controlEncoding"            value="UTF-8"/&gt;
     *                 &lt;property name="serverLanguageCode"         value="ko_KR"/&gt;
     *                 &lt;property name="serverKey"                  value="MS949"/&gt;
     *                 &lt;property name="debugCommandAndReply"       value="false"/&gt;
     *             &lt;/bean&gt;
     *         &lt;/list&gt;
     *     &lt;/property&gt;
     * &lt;/bean&gt;
     * </pre>
     * <br>
     * Example : {@link StorageManager}, {@code Service-Strategy}
     * <br>
     * {@code MOVE_FTP} service-strategy 는 {@code sourceAlias}가 <span style="color: black; background-color: #e69ed1;">SRC</span>인
     * FTP server에 접속하여  {@code directoryType}이 {@code REMOTE_SEND} 인 디렉터리 경로에 있는 파일을<br>{@code sourceAlias}가 <span style="color: black; background-color: #b1c4f0;">TGT</span>인
     * FTP server의 {@code directoryType}이 {@code REMOTE_RECEIVE} 인 디렉터리 경로에 업로드하는 프로세스가 정의되어있다.
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;!-- StorageManager --&gt;
     * &lt;bean class="mb.dnm.storage.StorageManager"&gt;
     *     &lt;property name="defaultInterfaceEnabled" value="true"/&gt;
     *     &lt;property name="interfaceRegistry"       ref="interfaces"/&gt;
     *     &lt;property name="serviceRegistry"         ref="serviceStrategies"/&gt;
     *     &lt;property name="errorHandlerRegistry"    ref="errorHandlers"/&gt;
     * &lt;/bean&gt;
     *
     * &lt;!-- Service-Strategy --&gt;
     * &lt;util:map id="serviceStrategies"&gt;
     *
     *     &lt;entry key="MOVE_FTP"&gt;
     *         &lt;description&gt;FTP to FTP Move&lt;/description&gt;
     *         &lt;list&gt;
     *             &lt;bean class="mb.dnm.service.ftp.ListFiles"&gt;
     *                 &lt;property name="description"        value="FTP 서버에서 다운로드할 파일목록을 확인한다."/&gt;
     *                 &lt;property name="sourceAlias"        value="<span style="color: black; background-color: #e69ed1;">SRC</span>"/&gt;
     *                 &lt;property name="directoryType"      value="REMOTE_SEND"/&gt;
     *                 &lt;property name="output"             value="remote_file_list"/&gt;
     *             &lt;/bean&gt;
     *                         .
     *                         .
     *                         .
     *             &lt;bean class="mb.dnm.service.ftp.DownloadFiles"&gt;
     *                 &lt;property name="description"        value="연계 대상 파일을 Indigo 연계서버(로컬)로 다운로드한다."/&gt;
     *                 &lt;property name="input"              value="remote_file_list"/&gt;
     *                 &lt;property name="sourceAlias"        value="<span style="color: black; background-color: #e69ed1;">SRC</span>"/&gt;
     *                 &lt;property name="directoryType"      value="LOCAL_TEMP"/&gt;
     *                 &lt;property name="output"             value="local_file_list"/&gt;
     *             &lt;/bean&gt;
     *                         .
     *                         .
     *                         .
     *             &lt;bean class="mb.dnm.service.ftp.UploadFiles"&gt;
     *                 &lt;property name="description"        value="생성한 파일을 FTP 서버에 업로드 한다."/&gt;
     *                 &lt;property name="input"              value="local_file_list"/&gt;
     *                 &lt;property name="sourceAlias"        value="<span style="color: black; background-color: #b1c4f0;">TGT</span>"/&gt;
     *                 &lt;property name="directoryType"      value="REMOTE_RECEIVE"/&gt;
     *             &lt;/bean&gt;
     *                         .
     *                         .
     *                         .
     *         &lt;/list&gt;
     *     &lt;/entry&gt;
     *                         .
     *                         .
     *                         .
     * &lt;/util:map&gt;
     * </pre>
     * <br>
     * <br>
     * Example : {@link InterfaceInfo}, {@link FileTemplate}<br>
     * {@code InterfaceInfo}에 등록된 {@code FileTemplate} 설정의 {@code templateName} 속성은
     * {@code FTPSourceProvider}에 등록된 {@code FTPClientTemplate} 중 사용할 {@code FTPClientTemplate}의
     * {@code templateName}과 동일하게 설정한다.
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.storage.InterfaceInfo"&gt;
     *     &lt;property name="interfaceId"                value="IF_ELD_to_HEZ"/&gt;
     *     			  .
     *     			  .
     *     			  .
     *     &lt;property name="sourceAliases"              value="<span style="color: black; background-color: #e69ed1;">SRC</span></span>:<span style="color: black; background-color: #FAF3D4;">ELD_FTP_SERVER</span>, <span style="color: black; background-color: #b1c4f0;">TGT</span>:<span style="color: black; background-color: #FAF3D4;">HEZ_FTP_SERVER</span>"/&gt;
     *     &lt;property name="serviceId"                  value="MOVE_FTP"/&gt;
     *     &lt;property name="fileTemplates"&gt;
     *         &lt;list&gt;
     *             &lt;bean class="mb.dnm.access.file.FileTemplate"&gt;
     *                 &lt;property name="templateName"        value="<span style="color: black; background-color: #FAF3D4;">ELD_FTP_SERVER</span>"/&gt;
     *                 &lt;property name="remoteSendDir"       value="/rec/cur"/&gt; &lt;!-- REMOTE_SEND --&gt;
     *                 &lt;property name="remoteSuccessDir"    value="/rec/cur/succ/@{YYYYMMDD}"/&gt; &lt;!-- REMOTE_SUCCESS --&gt;
     *                 &lt;property name="remoteErrorDir"      value="/rec/cur/err/@{YYYYMMDD}"/&gt; &lt;!-- REMOTE_ERROR --&gt;
     *                 &lt;property name="localTempDir"        value="/app/indigo/FILE_WORK/ELD/@{if_id}"/&gt; &lt;!-- LOCAL_TEMP --&gt;
     *                 &lt;property name="fileNamePattern"     value="RA0*"/&gt;
     *                 &lt;property name="type"                value="FILE"/&gt;
     *                 &lt;property name="dataType"            value="STRING"/&gt;
     *                 &lt;property name="charset"             value="MS949"/&gt;
     *             &lt;/bean&gt;
     *             &lt;bean class="mb.dnm.access.file.FileTemplate"&gt;
     *                 &lt;property name="templateName"        value="<span style="color: black; background-color: #FAF3D4;">HEZ_FTP_SERVER</span>"/&gt;
     *                 &lt;property name="remoteReceiveDir"    value="/sen/cur"/&gt; &lt;!-- REMOTE_RECEIVE --&gt;
     *                 &lt;property name="localTempDir"        value="/app/indigo/FILE_WORK/HEZ/@{if_id}"/&gt; &lt;!-- LOCAL_TEMP --&gt;
     *                 &lt;property name="charset"             value="MS949"/&gt;
     *             &lt;/bean&gt;
     *         &lt;/list&gt;
     *     &lt;/property&gt;
     * &lt;/bean&gt;
     *
     * &lt;bean class="mb.dnm.storage.InterfaceInfo"&gt;
     *     &lt;property name="interfaceId"                value="IF_HEZ_to_ELD"/&gt;
     *     			  .
     *     			  .
     *     			  .
     *     &lt;property name="sourceAliases"              value="<span style="color: black; background-color: #e69ed1;">SRC</span>:<span style="color: black; background-color: #FAF3D4;">HEZ_FTP_SERVER</span>, <span style="color: black; background-color: #b1c4f0;">TGT</span>:<span style="color: black; background-color: #FAF3D4;">ELD_FTP_SERVER</span>"/&gt;
     *     &lt;property name="serviceId"                  value="MOVE_FTP"/&gt;
     *     &lt;property name="fileTemplates"&gt;
     *         &lt;list&gt;
     *             &lt;bean class="mb.dnm.access.file.FileTemplate"&gt;
     *                 &lt;property name="templateName"        value="<span style="color: black; background-color: #FAF3D4;">HEZ_FTP_SERVER</span>"/&gt;
     *                 &lt;property name="remoteSendDir"       value="/rec/cur"/&gt; &lt;!-- REMOTE_SEND --&gt;
     *                 &lt;property name="remoteSuccessDir"    value="/rec/cur/succ/@{YYYYMMDD}"/&gt; &lt;!-- REMOTE_SUCCESS --&gt;
     *                 &lt;property name="remoteErrorDir"      value="/rec/cur/err/@{YYYYMMDD}"/&gt; &lt;!-- REMOTE_ERROR --&gt;
     *                 &lt;property name="localTempDir"        value="/app/indigo/FILE_WORK/HEZ/@{if_id}"/&gt; &lt;!-- LOCAL_TEMP --&gt;
     *                 &lt;property name="fileNamePattern"     value="RA0*"/&gt;
     *                 &lt;property name="type"                value="FILE"/&gt;
     *                 &lt;property name="dataType"            value="STRING"/&gt;
     *                 &lt;property name="charset"             value="MS949"/&gt;
     *             &lt;/bean&gt;
     *             &lt;bean class="mb.dnm.access.file.FileTemplate"&gt;
     *                 &lt;property name="templateName"        value="<span style="color: black; background-color: #FAF3D4;">ELD_FTP_SERVER</span>"/&gt;
     *                 &lt;property name="remoteReceiveDir"    value="/sen/cur"/&gt; &lt;!-- REMOTE_RECEIVE --&gt;
     *                 &lt;property name="localTempDir"        value="/app/indigo/FILE_WORK/ELD/@{if_id}"/&gt; &lt;!-- LOCAL_TEMP --&gt;
     *                 &lt;property name="charset"             value="MS949"/&gt;
     *             &lt;/bean&gt;
     *         &lt;/list&gt;
     *     &lt;/property&gt;
     * &lt;/bean&gt;</pre>
     *
     * @param aliasExpression the alias expression
     * @see mb.dnm.service.SourceAccessService
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
     * 알리아스를 사용하여 sourceName을 가져온다.<br>
     * Example : INTERFACE.xml
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.storage.InterfaceInfo"&gt;
     *     &lt;property name="interfaceId"                value="IF_ELD_to_HEZ"/&gt;
     *     			  .
     *     			  .
     *     			  .
     *     &lt;property name="sourceAliases"              value="<span style="color: black; background-color: #e69ed1;">SRC</span></span>:<span style="color: black; background-color: #FAF3D4;">ELD_FTP_SERVER</span>, <span style="color: black; background-color: #b1c4f0;">TGT</span>:<span style="color: black; background-color: #FAF3D4;">HEZ_FTP_SERVER</span>"/&gt;
     *     &lt;property name="serviceId"                  value="MOVE_FTP"/&gt;
     *     &lt;property name="fileTemplates"&gt;
     *         &lt;list&gt;
     *                      .
     *                      .
     *                      .
     *         &lt;/list&gt;
     *     &lt;/property&gt;
     * &lt;/bean&gt;</pre>
     * <br>
     * Example code : GetSourceNameByAliasExample.java
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *  InterfaceInfo info = StorageManager.access().getInterfaceInfo("IF_ELD_to_HEZ");
     *  String srcSource = info.getSourceNameByAlias("SRC");
     *  String tgtSource = info.getSourceNameByAlias("TGT");
     *
     *  System.out.println("Source FTP system: " + srcSource);
     *  System.out.println("Target FTP system: " + tgtSource);
     *
     *  >>Result :
     *      Source FTP system: ELD_FTP_SERVER
     *      Target FTP system: HEZ_FTP_SERVER</pre>
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
     * {@code InterfaceInfo}에 {@link FileTemplate} 을 등록한다.
     *
     * @param fileTemplates the file templates
     * @see FileTemplate
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
     * {@code InterfaceInfo} 에 {@link FileTemplate}을 등록할 때 지정했던 {@code templateName} 속성을 사용하여 {@link FileTemplate} 객체를 가져온다.
     *
     * @param templateName {@code FileTemplate}의 {@code templateName}
     * @return {@code FileTemplate} 객체
     * @see FileTemplate
     */
    public FileTemplate getFileTemplate(String templateName) {
        try {
            return fileTemplateMap.get(templateName);
        } catch (NullPointerException ne) {
            throw new NullPointerException("The file template map is not initialized.");
        }
    }

    /**
     * {@code InterfaceInfo}에 등록된 {@code querySequence} 배열(큐)를 가져온다.
     *
     * @return {@code querySequence} 배열(큐)
     */
    public String[] getQuerySequence() {
        return querySequenceArr;
    }

    /**
     * {@code InterfaceInfo}에 등록된 {@code errorQuerySequence} 배열(큐)를 가져온다.
     *
     * @return {@code errorQuerySequence} 배열(큐)
     */
    public String[] getErrorQuerySequence() {
        return errorQuerySequenceArr;
    }

    /**
     * {@code InterfaceInfo}의 HTTP 수신 Method 를 지정한다.
     * <br>
     * <p>
     * Example : INTERFACE.xml
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.storage.InterfaceInfo"&gt;
     *     &lt;property name="interfaceId"                value="IF_HTTP"/&gt;
     *     			  .
     *     			  .
     *     			  .
     *     &lt;property name="frontHttpUrl"               value="/v1/api/@{if_id}"/&gt;
     *     &lt;property name="frontHttpMethod"            value="POST"/&gt;
     * &lt;/bean&gt;</pre>
     * <p>
     * Example code : GetInterfaceInfoOfHttpRequest.java
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *  //A client sent an HTTP request to the /v1/api/IF_HTTP
     *  String requestPath = "/v1/api/IF_HTTP";
     *  String requestMethod = "POST";
     *
     *  InterfaceInfo info = StorageManager.access().getInterfaceInfoOfHttpRequest(requestPath, requestMethod);
     *  if (info == null) {
     *      //return HTTP Code 404(Not Found)
     *  } else {
     *      //Process interface
     *  }</pre>
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
     * {@code InterfaceInfo}의 HTTP 수신 URL 을 지정한다.
     * url에 @{if_id} 가 포함된 경우 interfaceId 속성으로 대체된다.<br>
     * <p>
     * Example : INTERFACE.xml
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * &lt;bean class="mb.dnm.storage.InterfaceInfo"&gt;
     *     &lt;property name="interfaceId"                value="IF_HTTP"/&gt;
     *     			  .
     *     			  .
     *     			  .
     *     &lt;property name="frontHttpUrl"               value="/v1/api/@{if_id}"/&gt;
     *     &lt;property name="frontHttpMethod"            value="POST"/&gt;
     * &lt;/bean&gt;</pre>
     * <p>
     * Example code : GetInterfaceInfoOfHttpRequest.java
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *  //A client sent an HTTP request to the /v1/api/IF_HTTP
     *  String requestPath = "/v1/api/IF_HTTP";
     *  String requestMethod = "POST";
     *
     *  InterfaceInfo info = StorageManager.access().getInterfaceInfoOfHttpRequest(requestPath, requestMethod);
     *  if (info == null) {
     *      //return HTTP Code 404(Not Found)
     *  } else {
     *      //Process interface
     *  }</pre>
     *
     * @param httpUrl the methods
     */
    public void setFrontHttpUrl(String httpUrl) {
        if (httpAPITemplate == null) {
            httpAPITemplate = new HttpAPITemplate();
        }
        httpAPITemplate.setFrontUrl(httpUrl);
    }

    /**
     * {@code InterfaceInfo} 에 등록된 HTTP URL을 가져온다
     *
     * @return the front http url
     */
    public String getFrontHttpUrl() {
        if (httpAPITemplate == null) {
            return null;
        }
        return httpAPITemplate.getFrontUrl();
    }

    /**
     * Gets interface id.
     *
     * @return the interface id
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * Gets interface name.
     *
     * @return the interface name
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets source code.
     *
     * @return the source code
     */
    public String getSourceCode() {
        return sourceCode;
    }

    /**
     * Gets target code.
     *
     * @return the target code
     */
    public String getTargetCode() {
        return targetCode;
    }

    /**
     * Gets service id.
     *
     * @return the service id
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Gets error handler id.
     *
     * @return the error handler id
     */
    public String getErrorHandlerId() {
        return errorHandlerId;
    }

    /**
     * Is activated boolean.
     *
     * @return the boolean
     */
    public boolean isActivated() {
        return activated;
    }
}
