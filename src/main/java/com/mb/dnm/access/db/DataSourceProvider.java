package com.mb.dnm.access.db;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DataSourceProvider {
    private static DataSourceProvider instance;
    private Map<String, QueryExecutor> executorMap;
    private boolean initilized = false;

    private DataSourceProvider() {
        if (instance == null) {
            instance = this;
            instance.executorMap = new HashMap<>();
        }
    }

    public static DataSourceProvider access() {
        if (instance == null) {
            new DataSourceProvider();
        }
        return instance;
    }


    public void setQueryExecutors(List<ExecutorFactory> executors) throws Exception {
        if (!initilized) {
            for (ExecutorFactory executorFactory : executors) {
                String name = executorFactory.getName();
                if (name == null || name.isEmpty()) {
                    throw new IllegalArgumentException("executorFactory name is null or empty");
                }
                if (executorMap.containsKey(name)) {
                    throw new IllegalArgumentException("duplicate executorFactory name: " + name);
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

                QueryExecutor executor = new QueryExecutor(sqlFactoryBean.getObject(), dataSource);
                executorMap.put(name, executor);
            }
            initilized = true;
            return;
        }
        throw new IllegalStateException("DataSourceProvider is already initialized");
    }

    public QueryExecutor getExecutor(String dsName) {
        return executorMap.get(dsName);
    }

    @Setter @Getter
    public class ExecutorFactory {
        private String name;
        private DataSource dataSource;
        private Resource configLocation;
        private Resource[] mapperLocations;

    }


}
