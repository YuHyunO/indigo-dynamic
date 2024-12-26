package mb.dnm.access.db;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class QueryMap implements Serializable {
    private static final long serialVersionUID = 6992066094805880704L;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryMap{");
        sb.append("executorName='").append(executorName).append('\'');
        sb.append(", queryId='").append(queryId).append('\'');
        sb.append(", timeoutSecond=").append(timeoutSecond);
        sb.append('}');
        return sb.toString();
    }
}
