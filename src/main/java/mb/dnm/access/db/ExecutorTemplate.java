package mb.dnm.access.db;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Setter
@Getter
@Slf4j
public class ExecutorTemplate {
    public ExecutorTemplate() {}

    private String templateName;
    private DataSource dataSource;
    private Resource configLocation;
    private Resource[] mapperLocations;
    private int defaultPatchSize = 65535;

    public String getName() {
        return templateName;
    }

    public void setMapperLocations(Resource[] mapperLocations) throws Exception {
        this.mapperLocations = mapperLocations;
    }

}