package mb.dnm.access.db;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.Serializable;


/**
 * {@code ExecutorTemplate}은 {@link QueryExecutor}를 생성하기 위해 사용되는 템플릿 객체이다.<br><br>
 *
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 *&lt;bean class="mb.dnm.access.db.ExecutorTemplate"&gt;
 *    &lt;property name="templateName"       value="<span style="color: black; background-color: #FAF3D4;">Executor 명</span>"/&gt;
 *    &lt;property name="dataSource"         ref="<span style="color: black; background-color: #FAF3D4;">DataSource 아이디</span>"/&gt;
 *    &lt;property name="configLocation"     value="mybatis-configuration.xml"/&gt;
 *    &lt;property name="mapperLocations"    value="classpath*:SQL_*.xml"/&gt;
 *&lt;/bean&gt;</pre>
 *
 * @author Yuhyun O
 */
@Setter
@Getter
@Slf4j
public class ExecutorTemplate implements Serializable {
    private static final long serialVersionUID = -4047923150248091356L;

    /**
     * Instantiates a new Executor template.
     */
    public ExecutorTemplate() {}

    /**
     * {@link DataSourceProvider} 에 등록될 {@link QueryExecutor} 객체의 이름
     * */
    private String templateName;
    /**
     * {@link QueryExecutor} 가 참조할 {@link DataSource} 객체
     * */
    private DataSource dataSource;

    private Resource configLocation;
    /**
     * Mybatis 매퍼 파일 위치
     * */
    private Resource[] mapperLocations;

    private int defaultFetchSize = 65535;

    /**
     * {@code ExecutorTemplate}의 이름을 반환한다.
     *
     * @return the name
     */
    public String getName() {
        return templateName;
    }

    /**
     * Sets mapper locations.
     *
     * @param mapperLocations the mapper locations
     * @throws Exception the exception
     */
    public void setMapperLocations(Resource[] mapperLocations) throws Exception {
        this.mapperLocations = mapperLocations;
    }

}