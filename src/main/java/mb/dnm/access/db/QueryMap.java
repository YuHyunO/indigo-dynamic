package mb.dnm.access.db;

import lombok.Getter;

import java.io.Serializable;


/**
 * 실행할 쿼리에 대한 메타데이터를 저장하는 객체이다.
 *
 * @author Yuhyun O
 */
@Getter
public class QueryMap implements Serializable {
    private static final long serialVersionUID = 6992066094805880704L;
    private String executorName;
    private String queryId;
    private int timeoutSecond = -1;

    /**
     * Instantiates a new Query map.
     *
     * @param executorName the executor name
     * @param queryId      the query id
     */
    public QueryMap(String executorName, String queryId) {
        this.executorName = executorName;
        this.queryId = queryId;
    }

    /**
     * Sets timeout second.
     *
     * @param timeoutSecond the timeout second
     */
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
