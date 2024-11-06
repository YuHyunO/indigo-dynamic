package mb.dnm.access.db;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.Serializable;

@Setter
@Getter
@Slf4j
public class ExecutorTemplate implements Serializable {
    private static final long serialVersionUID = -4047923150248091356L;

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