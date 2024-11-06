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


@Slf4j
public class DataSourceProvider implements Serializable {
    private static final long serialVersionUID = -3906433097076484909L;
    private static DataSourceProvider instance;
    private Map<String, QueryExecutor> executorMap;
    private Map<String, DataSourceTransactionManager> txManagers;
    private boolean initialized = false;

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

    public static DataSourceProvider access() {
        if (instance == null) {
            new DataSourceProvider();
        }
        return instance;
    }


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

    public QueryExecutor getExecutor(String executorName) {
        return executorMap.get(executorName);
    }

    public DataSourceTransactionManager getTransactionManager(String txManagerName) {
        return txManagers.get(txManagerName);
    }

}
