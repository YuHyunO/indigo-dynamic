package mb.dnm.access.db;

import lombok.Getter;

@Getter
public class QueryMap {
    private String executorName;
    private String queryId;

    public QueryMap(String executorName, String queryId) {
        this.executorName = executorName;
        this.queryId = queryId;
    }

}
