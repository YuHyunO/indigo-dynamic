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

/**
 * The type Transaction context.
 */
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


    /**
     * Instantiates a new Transaction context.
     *
     * @param name the name
     */
    TransactionContext(String name) {
        this.name = name;
        queryHistory = new ArrayList<>();
        lastTxStatus = new LastTransactionStatus();
    }

    /**
     * Add query history.
     *
     * @param queryId the query id
     */
    public void addQueryHistory(String queryId) {
        queryHistory.add(queryId);
    }

    /**
     * Gets query history.
     *
     * @return the query history
     */
    public List<String> getQueryHistory() {
        List<String> result = new ArrayList<>();
        for (String queryId : queryHistory) {
            result.add(queryId);
        }
        return result;
    }

    /**
     * Gets query history msg.
     *
     * @return the query history msg
     */
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

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets transaction status.
     *
     * @param txStatus the tx status
     */
    public void setTransactionStatus(TransactionStatus txStatus) {
       this.txStatus = txStatus;
    }

    /**
     * Gets transaction status.
     *
     * @return the transaction status
     */
    public TransactionStatus getTransactionStatus() {
        return txStatus;
    }

    /**
     * Sets group tx enabled.
     *
     * @param groupTxEnabled the group tx enabled
     */
    public void setGroupTxEnabled(boolean groupTxEnabled) {
        this.groupTxEnabled = groupTxEnabled;
    }

    /**
     * Sets transaction definition.
     *
     * @param txDef the tx def
     */
    public void setTransactionDefinition(DefaultTransactionDefinition txDef) {
        this.txDef = txDef;
    }

    /**
     * Gets transaction definition.
     *
     * @return the transaction definition
     */
    public DefaultTransactionDefinition getTransactionDefinition() {
        return txDef;
    }

    /**
     * Is group tx enabled boolean.
     *
     * @return the boolean
     */
    public boolean isGroupTxEnabled() {
        return groupTxEnabled;
    }

    /**
     * Sets timeout second.
     *
     * @param timeoutSecond the timeout second
     */
    public void setTimeoutSecond(int timeoutSecond) {
        this.timeoutSecond = timeoutSecond;
    }

    /**
     * Gets timeout second.
     *
     * @return the timeout second
     */
    public int getTimeoutSecond() {
        return timeoutSecond;
    }


    /**
     * Sets error.
     *
     * @param error the error
     */
    public void setError(Throwable error) {
        this.error = error;
    }

    /**
     * Gets error.
     *
     * @return the error
     */
    public Throwable getError() {
        return error;
    }

    /**
     * Sets last transaction status.
     */
    public void setLastTransactionStatus() {
        lastTxStatus.setLastTxStatus();
    }


    /**
     * The type Last transaction status.
     */
    @Getter
    public class LastTransactionStatus {
        private boolean initialized = false;
        private boolean actualTransactionActive = false;
        private Integer currentTransactionIsolationLevel = null;
        private boolean currentTransactionReadOnly = false;
        private String currentTransactionName = null;
        private List<TransactionSynchronization> synchronizations;

        /**
         * Sets last tx status.
         */
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
