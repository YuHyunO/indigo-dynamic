package mb.dnm.core.context;

import org.springframework.transaction.TransactionStatus;

public class TransactionContext {
    private String name;
    private boolean groupTxEnabled = false;
    private TransactionStatus txStatus;
    private int timeoutSecond = -1;
    private StringBuffer queryHistory;
    private Throwable error;

    TransactionContext(String name) {
        this.name = name;
        queryHistory = new StringBuffer();
    }

    public void addQueryHistory(String queryId) {
        if (queryHistory.length() != 0) {
            queryHistory.append('→').append(queryId);
        } else {
            queryHistory.append(queryId);
        }
    }

    public String getQueryHistory() {
        return queryHistory.toString();
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
