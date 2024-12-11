package mb.dnm.core.context;

import lombok.Getter;
import lombok.Setter;
import mb.dnm.util.MessageUtil;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransactionContext implements Serializable {
    private static final long serialVersionUID = -1311345572730756673L;
    private String name;
    private boolean groupTxEnabled = false;
    private TransactionStatus txStatus;
    private int timeoutSecond = -1;
    private DefaultTransactionDefinition txDef;
    private List<String> queryHistory;
    private Throwable error;
    @Setter @Getter
    private boolean constant = false;
    @Getter
    private LastTransactionStatus lastTxStatus;



    TransactionContext(String name) {
        this.name = name;
        queryHistory = new ArrayList<>();
        lastTxStatus = new LastTransactionStatus();
    }

    public void addQueryHistory(String queryId) {
        queryHistory.add(queryId);
    }

    public List<String> getQueryHistory() {
        List<String> result = new ArrayList<>();
        for (String queryId : queryHistory) {
            result.add(queryId);
        }
        return result;
    }

    public String getQueryHistoryMsg() {
        StringBuilder msg = new StringBuilder();
        int i = 1;
        for (String queryId : queryHistory) {
            msg.append("(").append(i).append(")").append(queryId).append(",").append(" ");
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

    public void setTransactionDefinition(DefaultTransactionDefinition txDef) {
        this.txDef = txDef;
    }

    public DefaultTransactionDefinition getTransactionDefinition() {
        return txDef;
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

    public void setLastTransactionStatus() {
        lastTxStatus.setLastTxStatus();
    }


    @Getter
    public class LastTransactionStatus {
        private boolean initialized = false;
        private boolean actualTransactionActive = false;
        private Integer currentTransactionIsolationLevel = null;
        private boolean currentTransactionReadOnly = false;
        private String currentTransactionName = null;
        private List<TransactionSynchronization> synchronizations;

        public void setLastTxStatus() {
            actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            currentTransactionIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
            currentTransactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                synchronizations = TransactionSynchronizationManager.getSynchronizations();
            }
            initialized = true;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("LastTransactionStatus{");
            sb.append("initialized=").append(initialized);
            sb.append(", actualTransactionActive=").append(actualTransactionActive);
            sb.append(", currentTransactionIsolationLevel=").append(currentTransactionIsolationLevel);
            sb.append(", currentTransactionReadOnly=").append(currentTransactionReadOnly);
            sb.append(", currentTransactionName='").append(currentTransactionName).append('\'');
            sb.append(", synchronizations=").append(synchronizations);
            sb.append('}');
            return sb.toString();
        }
    }
}
