package mb.dnm.access.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Setter
@Getter
public class ExecutorTemplate {
    public ExecutorTemplate() {}

    private String templateName;
    private DataSource dataSource;
    private Resource configLocation;
    private Resource[] mapperLocations;

    public String getName() {
        return templateName;
    }

}