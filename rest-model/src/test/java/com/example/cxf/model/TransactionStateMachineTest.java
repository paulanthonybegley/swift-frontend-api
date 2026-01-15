package com.example.cxf.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for TransactionStateMachine covering all valid and
 * invalid transitions.
 */
class TransactionStateMachineTest {

    // ========== Valid Transition Tests ==========

    @Test
    @DisplayName("INIT → PDNG transition should be valid")
    void testInitToPending() {
        assertTrue(TransactionStateMachine.canTransition(TransactionState.INIT, TransactionState.PDNG));
        assertEquals(TransactionState.PDNG,
                TransactionStateMachine.transition(TransactionState.INIT, TransactionState.PDNG));
    }

    @Test
    @DisplayName("INIT → RJCT transition should be valid (early rejection)")
    void testInitToRejected() {
        assertTrue(TransactionStateMachine.canTransition(TransactionState.INIT, TransactionState.RJCT));
        assertEquals(TransactionState.RJCT,
                TransactionStateMachine.transition(TransactionState.INIT, TransactionState.RJCT));
    }

    @Test
    @DisplayName("PDNG → ACCC transition should be valid (successful completion)")
    void testPendingToAccepted() {
        assertTrue(TransactionStateMachine.canTransition(TransactionState.PDNG, TransactionState.ACCC));
        assertEquals(TransactionState.ACCC,
                TransactionStateMachine.transition(TransactionState.PDNG, TransactionState.ACCC));
    }

    @Test
    @DisplayName("PDNG → RJCT transition should be valid (processing failure)")
    void testPendingToRejected() {
        assertTrue(TransactionStateMachine.canTransition(TransactionState.PDNG, TransactionState.RJCT));
        assertEquals(TransactionState.RJCT,
                TransactionStateMachine.transition(TransactionState.PDNG, TransactionState.RJCT));
    }

    @Test
    @DisplayName("PDNG → PDNG transition should be valid (adding extra routes)")
    void testPendingToPending() {
        assertTrue(TransactionStateMachine.canTransition(TransactionState.PDNG, TransactionState.PDNG));
        assertEquals(TransactionState.PDNG,
                TransactionStateMachine.transition(TransactionState.PDNG, TransactionState.PDNG));
    }

    @Test
    @DisplayName("ACCC → ACCC transition should be valid (terminal state idempotence)")
    void testAcceptedToAccepted() {
        assertTrue(TransactionStateMachine.canTransition(TransactionState.ACCC, TransactionState.ACCC));
        assertEquals(TransactionState.ACCC,
                TransactionStateMachine.transition(TransactionState.ACCC, TransactionState.ACCC));
    }

    @Test
    @DisplayName("RJCT → RJCT transition should be valid (terminal state idempotence)")
    void testRejectedToRejected() {
        assertTrue(TransactionStateMachine.canTransition(TransactionState.RJCT, TransactionState.RJCT));
        assertEquals(TransactionState.RJCT,
                TransactionStateMachine.transition(TransactionState.RJCT, TransactionState.RJCT));
    }

    // ========== Invalid Transition Tests ==========

    @Test
    @DisplayName("INIT → ACCC transition should be invalid (must go through PDNG)")
    void testInitToAcceptedInvalid() {
        assertFalse(TransactionStateMachine.canTransition(TransactionState.INIT, TransactionState.ACCC));
        assertThrows(IllegalStateException.class,
                () -> TransactionStateMachine.transition(TransactionState.INIT, TransactionState.ACCC));
    }

    @Test
    @DisplayName("ACCC → PDNG transition should be invalid (terminal state)")
    void testAcceptedToPendingInvalid() {
        assertFalse(TransactionStateMachine.canTransition(TransactionState.ACCC, TransactionState.PDNG));
        assertThrows(IllegalStateException.class,
                () -> TransactionStateMachine.transition(TransactionState.ACCC, TransactionState.PDNG));
    }

    @Test
    @DisplayName("ACCC → RJCT transition should be invalid (terminal state)")
    void testAcceptedToRejectedInvalid() {
        assertFalse(TransactionStateMachine.canTransition(TransactionState.ACCC, TransactionState.RJCT));
        assertThrows(IllegalStateException.class,
                () -> TransactionStateMachine.transition(TransactionState.ACCC, TransactionState.RJCT));
    }

    @Test
    @DisplayName("ACCC → INIT transition should be invalid (terminal state)")
    void testAcceptedToInitInvalid() {
        assertFalse(TransactionStateMachine.canTransition(TransactionState.ACCC, TransactionState.INIT));
        assertThrows(IllegalStateException.class,
                () -> TransactionStateMachine.transition(TransactionState.ACCC, TransactionState.INIT));
    }

    @Test
    @DisplayName("RJCT → INIT transition should be invalid (terminal state)")
    void testRejectedToInitInvalid() {
        assertFalse(TransactionStateMachine.canTransition(TransactionState.RJCT, TransactionState.INIT));
        assertThrows(IllegalStateException.class,
                () -> TransactionStateMachine.transition(TransactionState.RJCT, TransactionState.INIT));
    }

    @Test
    @DisplayName("RJCT → PDNG transition should be invalid (terminal state)")
    void testRejectedToPendingInvalid() {
        assertFalse(TransactionStateMachine.canTransition(TransactionState.RJCT, TransactionState.PDNG));
        assertThrows(IllegalStateException.class,
                () -> TransactionStateMachine.transition(TransactionState.RJCT, TransactionState.PDNG));
    }

    @Test
    @DisplayName("RJCT → ACCC transition should be invalid (terminal state)")
    void testRejectedToAcceptedInvalid() {
        assertFalse(TransactionStateMachine.canTransition(TransactionState.RJCT, TransactionState.ACCC));
        assertThrows(IllegalStateException.class,
                () -> TransactionStateMachine.transition(TransactionState.RJCT, TransactionState.ACCC));
    }

    @Test
    @DisplayName("PDNG → INIT transition should be invalid (no backwards transitions)")
    void testPendingToInitInvalid() {
        assertFalse(TransactionStateMachine.canTransition(TransactionState.PDNG, TransactionState.INIT));
        assertThrows(IllegalStateException.class,
                () -> TransactionStateMachine.transition(TransactionState.PDNG, TransactionState.INIT));
    }

    // ========== End-to-End Flow Tests ==========

    @Test
    @DisplayName("Normal flow: INIT → PDNG → ACCC should succeed")
    void testNormalFlowEndToEnd() {
        TransactionState state = TransactionState.INIT;

        // INIT → PDNG
        state = TransactionStateMachine.transition(state, TransactionState.PDNG);
        assertEquals(TransactionState.PDNG, state);

        // PDNG → ACCC
        state = TransactionStateMachine.transition(state, TransactionState.ACCC);
        assertEquals(TransactionState.ACCC, state);

        // Verify terminal
        assertTrue(TransactionStateMachine.isTerminal(state));
    }

    @Test
    @DisplayName("Early rejection flow: INIT → RJCT should succeed")
    void testEarlyRejectionFlow() {
        TransactionState state = TransactionState.INIT;

        // INIT → RJCT
        state = TransactionStateMachine.transition(state, TransactionState.RJCT);
        assertEquals(TransactionState.RJCT, state);

        // Verify terminal
        assertTrue(TransactionStateMachine.isTerminal(state));
    }

    @Test
    @DisplayName("Processing failure flow: INIT → PDNG → RJCT should succeed")
    void testProcessingFailureFlow() {
        TransactionState state = TransactionState.INIT;

        // INIT → PDNG
        state = TransactionStateMachine.transition(state, TransactionState.PDNG);
        assertEquals(TransactionState.PDNG, state);

        // PDNG → RJCT
        state = TransactionStateMachine.transition(state, TransactionState.RJCT);
        assertEquals(TransactionState.RJCT, state);

        // Verify terminal
        assertTrue(TransactionStateMachine.isTerminal(state));
    }

    // ========== Terminal State Tests ==========

    @Test
    @DisplayName("ACCC should be identified as terminal")
    void testAcceptedIsTerminal() {
        assertTrue(TransactionStateMachine.isTerminal(TransactionState.ACCC));
        assertTrue(TransactionState.ACCC.isTerminal());
    }

    @Test
    @DisplayName("RJCT should be identified as terminal")
    void testRejectedIsTerminal() {
        assertTrue(TransactionStateMachine.isTerminal(TransactionState.RJCT));
        assertTrue(TransactionState.RJCT.isTerminal());
    }

    @Test
    @DisplayName("INIT should not be terminal")
    void testInitNotTerminal() {
        assertFalse(TransactionStateMachine.isTerminal(TransactionState.INIT));
        assertFalse(TransactionState.INIT.isTerminal());
    }

    @Test
    @DisplayName("PDNG should not be terminal")
    void testPendingNotTerminal() {
        assertFalse(TransactionStateMachine.isTerminal(TransactionState.PDNG));
        assertFalse(TransactionState.PDNG.isTerminal());
    }

    // ========== Valid Next States Tests ==========

    @Test
    @DisplayName("INIT should have PDNG and RJCT as valid next states")
    void testInitValidNextStates() {
        Set<TransactionState> validStates = TransactionStateMachine.getValidNextStates(TransactionState.INIT);
        assertEquals(2, validStates.size());
        assertTrue(validStates.contains(TransactionState.PDNG));
        assertTrue(validStates.contains(TransactionState.RJCT));
    }

    @Test
    @DisplayName("PDNG should have PDNG, ACCC and RJCT as valid next states")
    void testPendingValidNextStates() {
        Set<TransactionState> validStates = TransactionStateMachine.getValidNextStates(TransactionState.PDNG);
        assertEquals(3, validStates.size());
        assertTrue(validStates.contains(TransactionState.PDNG));
        assertTrue(validStates.contains(TransactionState.ACCC));
        assertTrue(validStates.contains(TransactionState.RJCT));
    }

    @Test
    @DisplayName("ACCC should only have ACCC as valid next state (terminal)")
    void testAcceptedValidNextStates() {
        Set<TransactionState> validStates = TransactionStateMachine.getValidNextStates(TransactionState.ACCC);
        assertEquals(1, validStates.size());
        assertTrue(validStates.contains(TransactionState.ACCC));
    }

    @Test
    @DisplayName("RJCT should only have RJCT as valid next state (terminal)")
    void testRejectedValidNextStates() {
        Set<TransactionState> validStates = TransactionStateMachine.getValidNextStates(TransactionState.RJCT);
        assertEquals(1, validStates.size());
        assertTrue(validStates.contains(TransactionState.RJCT));
    }

    // ========== Null Handling Tests ==========

    @Test
    @DisplayName("Null from state should return false for canTransition")
    void testNullFromState() {
        assertFalse(TransactionStateMachine.canTransition(null, TransactionState.PDNG));
    }

    @Test
    @DisplayName("Null to state should return false for canTransition")
    void testNullToState() {
        assertFalse(TransactionStateMachine.canTransition(TransactionState.INIT, null));
    }

    @Test
    @DisplayName("Null state should return false for isTerminal")
    void testNullIsTerminal() {
        assertFalse(TransactionStateMachine.isTerminal(null));
    }

    @Test
    @DisplayName("Null state should return empty set for getValidNextStates")
    void testNullValidNextStates() {
        Set<TransactionState> validStates = TransactionStateMachine.getValidNextStates(null);
        assertTrue(validStates.isEmpty());
    }
}
