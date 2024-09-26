package mb.dnm.core.context;

import mb.dnm.util.MessageUtil;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.List;

public class TransactionContext {
    private String name;
    private boolean groupTxEnabled = false;
    private TransactionStatus txStatus;
    private int timeoutSecond = -1;
    private List<StringBuilder> queryHistory;
    private Throwable error;

    TransactionContext(String name) {
        this.name = name;
        queryHistory = new ArrayList<>();
    }

    public void addQueryHistory(String queryId) {
        queryHistory.add(new StringBuilder(queryId));
    }

    public void addQueryHistory(StringBuilder queryId) {
        queryHistory.add(queryId);
    }

    public List<String> getQueryHistory() {
        List<String> result = new ArrayList<>();
        for (StringBuilder queryId : queryHistory) {
            result.add(queryId.toString());
        }
        return result;
    }

    public String getQueryHistoryMsg() {
        StringBuilder msg = new StringBuilder();
        int i = 1;
        for (StringBuilder queryId : queryHistory) {
            msg.append("(").append(i).append("):").append(queryId).append(",").append(" ");
            ++i;
        }
        if (msg.length() > 0) {
            msg.setLength(msg.length() - " ".length());
        }
        return msg.toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTransactionStatus(TransactionStatus txStatus) {
       this.txStatus = txStatus;
    }

    public TransactionStatus getTransactionStatus() {
        return txStatus;
    }

    public void setGroupTxEnabled(boolean groupTxEnabled) {
        this.groupTxEnabled = groupTxEnabled;
    }

    public boolean isGroupTxEnabled() {
        return groupTxEnabled;
    }

    public void setTimeoutSecond(int timeoutSecond) {
        this.timeoutSecond = timeoutSecond;
    }

    public int getTimeoutSecond() {
        return timeoutSecond;
    }


    public void setError(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }
}
