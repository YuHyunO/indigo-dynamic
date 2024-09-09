package mb.dnm.service;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public abstract class SourceAccessService extends ParameterAssignableService {
    protected String sourceName;
    protected String sourceAlias;

}
