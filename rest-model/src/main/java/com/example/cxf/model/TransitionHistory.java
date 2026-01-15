package com.example.cxf.model;

import java.time.Instant;

/**
 * Represents a single transition in the transaction lifecycle.
 * Used for audit trail and history tracking.
 */
public class TransitionHistory {
    private final String uetr;
    private final TransactionState fromState;
    private final TransactionState toState;
    private final Instant timestamp;
    private final String apiData; // JSON or serialized data from API
    private final String reason; // Optional reason for transition

    public TransitionHistory(String uetr, TransactionState fromState, TransactionState toState,
            String apiData, String reason) {
        this.uetr = uetr;
        this.fromState = fromState;
        this.toState = toState;
        this.timestamp = Instant.now();
        this.apiData = apiData;
        this.reason = reason;
    }

    public String getUetr() {
        return uetr;
    }

    public TransactionState getFromState() {
        return fromState;
    }

    public TransactionState getToState() {
        return toState;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getApiData() {
        return apiData;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return String.format("TransitionHistory[uetr=%s, %s→%s, timestamp=%s, reason=%s]",
                uetr, fromState, toState, timestamp, reason);
    }
}
