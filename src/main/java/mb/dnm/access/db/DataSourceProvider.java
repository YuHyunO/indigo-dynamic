package mb.dnm.access.db;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * {@code DataSourceProvider}는 데이터베이스 접속 및 쿼리 작업을 수행하는 객체인 {@link QueryExecutor} 를 등록하여 사용하는 객체이다.<br><br>
 *
 * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
 *&lt;bean class="mb.dnm.access.db.DataSourceProvider"&gt;
 *    &lt;property name="queryExecutors"&gt;
 *        &lt;list&gt;
 *            &lt;bean class="mb.dnm.access.db.ExecutorTemplate"&gt;
 *                &lt;property name="templateName"       value="<span style="color: black; background-color: #FAF3D4;">Executor 명</span>"/&gt;
 *                &lt;property name="dataSource"         ref="<span style="color: black; background-color: #FAF3D4;">DataSource 아이디</span>"/&gt;
 *                &lt;property name="configLocation"     value="mybatis-configuration.xml"/&gt;
 *                &lt;property name="mapperLocations"    value="classpath*:SQL_*.xml"/&gt;
 *            &lt;/bean&gt;
 *                                             .
 *                                             .
 *                                             .
 *        &lt;/list&gt;
 *    &lt;/property&gt;
 *&lt;/bean&gt;</pre>
 *
 * @author Yuhyun O
 */
@Slf4j
public class DataSourceProvider implements Serializable {
    /**
     * @hide
     */
    private static final long serialVersionUID = -3906433097076484909L;
    private static DataSourceProvider instance;
    private Map<String, QueryExecutor> executorMap;
    private Map<String, DataSourceTransactionManager> txManagers;
    private boolean initialized = false;

    /**
     * Instantiates a new Data source provider.
     */
    /*
    * Spring version 만 맞다면 private 으로 변경해도 bean으로 등록 가능함
    * */
    public DataSourceProvider() {
        if (instance == null) {
            instance = this;
            instance.executorMap = new HashMap<>();
            instance.txManagers = new HashMap<>();
        }

    }

    /**
     * {@code DataSourceProvider} 인스턴스에 접근한다.
     *
     * @return the data source provider
     */
    public static DataSourceProvider access() {
        if (instance == null) {
            new DataSourceProvider();
        }
        return instance;
    }

    /**
     * {@link ExecutorTemplate} 을 통해 {@link QueryExecutor}를 생성하여 정의된 QueryExecutor 명으로 {@code DataSourceProvider} 인스턴스에 등록한다.
     *
     * @param executors the executors
     * @throws Exception the exception
     */
    public void setQueryExecutors(List<ExecutorTemplate> executors) throws Exception {
        if (!initialized) {
            for (ExecutorTemplate executorFactory : executors) {
                String name = executorFactory.getName();
                if (name == null || name.isEmpty()) {
                    throw new IllegalArgumentException("The executor template name is null or empty");
                }
                if (executorMap.containsKey(name)) {
                    throw new IllegalArgumentException("duplicate executor template name: " + name);
                }

                DataSource dataSource = executorFactory.getDataSource();
                if (dataSource == null) {
                    throw new IllegalArgumentException("dataSource is null");
                }

                SqlSessionFactoryBean sqlFactoryBean = new SqlSessionFactoryBean();

                sqlFactoryBean.setDataSource(dataSource);

                Resource configLocation = executorFactory.getConfigLocation();
                if (configLocation != null) {
                    sqlFactoryBean.setConfigLocation(configLocation);
                }

                Resource[] mapperLocations = executorFactory.getMapperLocations();
                if (mapperLocations != null) {
                    sqlFactoryBean.setMapperLocations(mapperLocations);
                }

                QueryExecutor executor = new QueryExecutor();

                DataSourceTransactionManager txManager = new DataSourceTransactionManager(dataSource);
                txManagers.put(name, txManager);
                executor.initialize(sqlFactoryBean.getObject());

                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(QueryExecutor.class);
                enhancer.setCallback(new TransactionProxyInterceptor(executor));
                QueryExecutor proxy = (QueryExecutor) enhancer.create();
                executorMap.put(name, proxy);
            }
            initialized = true;
            return;
        }
        throw new IllegalStateException("DataSourceProvider is already initialized");
    }

    /**
     * {@code DataSourceProvider} 인스턴스에서 QueryExecutor 명과 일치하는 {@link QueryExecutor}을 반환한다.
     *
     * @param executorName the executor name
     * @return the executor
     */
    public QueryExecutor getExecutor(String executorName) {
        return executorMap.get(executorName);
    }

    /**
     * {@code DataSourceProvider} 인스턴스에서 {@link QueryExecutor}를 관리하는 {@code DataSourceTransactionManager}를 반환한다.
     *
     * @param executorName the executor name
     * @return the transaction manager
     */
    public DataSourceTransactionManager getTransactionManager(String executorName) {
        return txManagers.get(executorName);
    }

}
