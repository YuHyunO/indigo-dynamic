package com.mb.dnm.core.context;

import org.springframework.transaction.TransactionStatus;

public class TransactionContext {
    private String name;
    private boolean groupTxEnabled = false;
    private boolean txStarted = false;
    private boolean txEnded = false;
    private TransactionStatus txStatus;
    private boolean committed = false;
    private boolean rollbacked = false;
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

    public void setTxStarted(boolean txStarted) {
        this.txStarted = txStarted;
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    public void setRollbacked(boolean rollbacked) {
        this.rollbacked = rollbacked;
    }

    public void setTxEnded(boolean txEnded) {
        this.txEnded = txEnded;
    }

    public boolean isGroupTxEnabled() {
        return groupTxEnabled;
    }

    public boolean isTxStarted() {
        return txStarted;
    }

    public boolean isCommitted() {
        return committed;
    }

    public boolean isRollbacked() {
        return rollbacked;
    }

    public boolean isTxEnded() {
        return txEnded;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }
}
