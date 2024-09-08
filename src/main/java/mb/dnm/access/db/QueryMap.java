package mb.dnm.access.db;

import lombok.Getter;

@Getter
public class QueryMap {
    private String executorName;
    private String queryId;
    private int timeoutSecond = -1;

    public QueryMap(String executorName, String queryId) {
        this.executorName = executorName;
        this.queryId = queryId;
    }

    public void setTimeoutSecond(int timeoutSecond) {
        this.timeoutSecond = timeoutSecond;
    }


}
