package com.example.cxf.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State machine for managing transaction lifecycle transitions.
 * 
 * Enforces valid state transitions and prevents invalid ones.
 * 
 * Valid transitions:
 * - INIT → PDNG (normal processing)
 * - INIT → RJCT (early rejection)
 * - PDNG → ACCC (successful completion)
 * - PDNG → RJCT (processing failure)
 * - ACCC → ACCC (terminal state, idempotent)
 * - RJCT → RJCT (terminal state, idempotent)
 */
public class TransactionStateMachine {

    private static final Map<TransactionState, Set<TransactionState>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(TransactionState.class);

        // INIT can transition to PDNG or RJCT
        VALID_TRANSITIONS.put(TransactionState.INIT,
                EnumSet.of(TransactionState.PDNG, TransactionState.RJCT));

        // PDNG can transition to ACCC, RJCT, or stay in PDNG (adding extra routes)
        VALID_TRANSITIONS.put(TransactionState.PDNG,
                EnumSet.of(TransactionState.PDNG, TransactionState.ACCC, TransactionState.RJCT));

        // ACCC is terminal (can only stay in ACCC)
        VALID_TRANSITIONS.put(TransactionState.ACCC,
                EnumSet.of(TransactionState.ACCC));

        // RJCT is terminal (can only stay in RJCT)
        VALID_TRANSITIONS.put(TransactionState.RJCT,
                EnumSet.of(TransactionState.RJCT));
    }

    /**
     * Checks if a transition from one state to another is valid.
     * 
     * @param from the current state
     * @param to   the target state
     * @return true if the transition is valid, false otherwise
     */
    public static boolean canTransition(TransactionState from, TransactionState to) {
        if (from == null || to == null) {
            return false;
        }

        Set<TransactionState> validNextStates = VALID_TRANSITIONS.get(from);
        return validNextStates != null && validNextStates.contains(to);
    }

    /**
     * Performs a state transition, throwing an exception if invalid.
     * 
     * @param from the current state
     * @param to   the target state
     * @return the target state if transition is valid
     * @throws IllegalStateException if the transition is invalid
     */
    public static TransactionState transition(TransactionState from, TransactionState to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition: %s -> %s", from, to));
        }
        return to;
    }

    /**
     * Gets all valid next states for a given state.
     * 
     * @param from the current state
     * @return set of valid next states
     */
    public static Set<TransactionState> getValidNextStates(TransactionState from) {
        if (from == null) {
            return EnumSet.noneOf(TransactionState.class);
        }

        Set<TransactionState> validStates = VALID_TRANSITIONS.get(from);
        return validStates != null ? EnumSet.copyOf(validStates) : EnumSet.noneOf(TransactionState.class);
    }

    /**
     * Returns true if the state is terminal (no further transitions possible except
     * to itself).
     * 
     * @param state the state to check
     * @return true if terminal
     */
    public static boolean isTerminal(TransactionState state) {
        if (state == null) {
            return false;
        }
        return state.isTerminal();
    }
}
