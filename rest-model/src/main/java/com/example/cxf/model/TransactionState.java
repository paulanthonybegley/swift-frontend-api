package com.example.cxf.model;

/**
 * Represents the possible states of a payment transaction.
 * 
 * State Transitions:
 * - INIT → PDNG (normal processing)
 * - INIT → RJCT (early rejection)
 * - PDNG → ACCC (successful completion)
 * - PDNG → RJCT (processing failure)
 * - ACCC → ACCC (terminal state, stable)
 * - RJCT → RJCT (terminal state, stable)
 */
public enum TransactionState {
    /**
     * Initial state - transaction has been created but not yet processed.
     */
    INIT("Initial Status", "Initialized"),

    /**
     * Pending state - transaction is being processed.
     */
    PDNG("Payment Pending", "Processing"),

    /**
     * Accepted/Credited state - transaction completed successfully (terminal).
     */
    ACCC("Payment credited to beneficiary bank", "Credited"),

    /**
     * Rejected state - transaction failed (terminal).
     */
    RJCT("Payment rejected", "Rejected");

    private final String description;
    private final String reason;

    TransactionState(String description, String reason) {
        this.description = description;
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public String getReason() {
        return reason;
    }

    /**
     * Returns true if this is a terminal state (ACCC or RJCT).
     */
    public boolean isTerminal() {
        return this == ACCC || this == RJCT;
    }
}
