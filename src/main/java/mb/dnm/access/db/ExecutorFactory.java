package mb.dnm.access.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Setter
@Getter
public class ExecutorFactory {
    public ExecutorFactory() {}

    private String name;
    private DataSource dataSource;
    private Resource configLocation;
    private Resource[] mapperLocations;

}