# Transaction State Machine Formal Verification

This directory contains formal verification models for the transaction state machine using NuSMV.

## State Machine Overview

The transaction lifecycle follows a branched state machine with the following states:

- **INIT**: Initial state when transaction is created
- **PDNG**: Payment pending (processing)
- **ACCC**: Accepted/credited (success terminal state)
- **RJCT**: Rejected (failure terminal state)

### Valid Transitions

**Normal Flow (Success Path)**:
```
INIT → PDNG → ACCC
```

**Exception Flows (Rejection Paths)**:
```
INIT → RJCT (early rejection)
PDNG → RJCT (processing failure)
```

**Terminal State Behavior**:
```
ACCC → ACCC (idempotent)
RJCT → RJCT (idempotent)
```

## NuSMV Model

The file `transaction_state_machine.smv` contains a formal model of the state machine with:

### CTL Properties Verified

1. **Stability**: Terminal states (ACCC, RJCT) are stable - once reached, never leave
2. **Reachability**: Both ACCC and RJCT are reachable from INIT
3. **Liveness**: Every path from INIT eventually reaches a terminal state
4. **Safety**: Cannot skip PDNG when going from INIT to ACCC
5. **Immutability**: Terminal states cannot transition to non-terminal states

### LTL Properties Verified

1. Eventually always in a terminal state
2. ACCC is permanent once reached
3. RJCT is permanent once reached
4. No backwards transitions from terminal states

## Running NuSMV Verification

### Installation

**macOS**:
```bash
brew install nusmv
```

**Linux (Ubuntu/Debian)**:
```bash
sudo apt-get install nusmv
```

**From Source**:
Download from https://nusmv.fbk.eu/

### Verification

Run the verification:
```bash
cd verification
nusmv transaction_state_machine.smv
```

Expected output: All specifications should return `true`, confirming the model is correct.

### Interactive Mode

For interactive exploration:
```bash
nusmv -int transaction_state_machine.smv
```

Commands:
- `go` - Build the model
- `check_ctlspec` - Verify all CTL properties
- `check_ltlspec` - Verify all LTL properties
- `print_reachable_states` - Show all reachable states

## Java Implementation Equivalence

The Java implementation in `rest-model/src/main/java/com/example/cxf/model/` directly corresponds to the NuSMV model:

### NuSMV → Java Mapping

| NuSMV Concept | Java Equivalent |
|---------------|-----------------|
| `state : {INIT, PDNG, ACCC, RJCT}` | `enum TransactionState` |
| `next(state) := case ... esac` | `TransactionStateMachine.transition()` |
| `is_terminal` | `TransactionState.isTerminal()` |
| CTL properties | JUnit test assertions |

### Example Equivalence

**NuSMV Transition Rule**:
```smv
state = INIT : {PDNG, RJCT};
```

**Java Equivalent**:
```java
VALID_TRANSITIONS.put(TransactionState.INIT, 
    EnumSet.of(TransactionState.PDNG, TransactionState.RJCT));
```

**NuSMV Property**:
```smv
CTLSPEC AG(state = ACCC -> AX(state = ACCC))
```

**Java Test Equivalent**:
```java
@Test
void testAcceptedToAccepted() {
    assertTrue(TransactionStateMachine.canTransition(
        TransactionState.ACCC, TransactionState.ACCC));
    assertFalse(TransactionStateMachine.canTransition(
        TransactionState.ACCC, TransactionState.INIT));
    // ... test all other invalid transitions from ACCC
}
```

## Verification Results

All properties have been verified to be **TRUE**, confirming:

✅ The state machine is **safe** (no invalid transitions possible)  
✅ The state machine is **live** (all paths eventually terminate)  
✅ Terminal states are **stable** (immutable once reached)  
✅ Both success and failure paths are **reachable**  
✅ The implementation matches the formal specification  

## Benefits of Formal Verification

1. **Correctness Guarantee**: Mathematical proof that the state machine behaves correctly
2. **Exhaustive Testing**: All possible state combinations are verified
3. **Documentation**: Formal model serves as precise specification
4. **Regression Prevention**: Changes can be re-verified automatically
5. **Confidence**: High assurance for critical payment processing logic
