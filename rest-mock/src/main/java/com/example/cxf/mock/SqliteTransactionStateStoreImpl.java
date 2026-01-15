package com.example.cxf.mock;

import com.example.cxf.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class SqliteTransactionStateStoreImpl implements TransactionStateStore {
    private final JdbcTemplate jdbcTemplate;
    private final TransitionHistoryStore historyStore;

    public SqliteTransactionStateStoreImpl(String dbPath) {
        DataSource dataSource = new DriverManagerDataSource("jdbc:sqlite:" + dbPath);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.historyStore = new TransitionHistoryStore(dbPath);
        initDb();
    }

    private void initDb() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transaction_states (" +
                "uetr TEXT PRIMARY KEY, " +
                "current_state TEXT NOT NULL, " +
                "call_count INTEGER DEFAULT 0)");
    }

    @Override
    public PaymentTransaction166 getOrUpdate(String uetr) {
        // Get current state or initialize
        TransactionState currentState = getCurrentState(uetr);
        TransactionState previousState = currentState;

        // Increment call counter
        int callCount = incrementCallCount(uetr);

        // Determine next state based on call count and current state
        TransactionState nextState = determineNextState(currentState, callCount);

        // Check if this is a meaningful transition (change of state OR PDNG->PDNG for
        // routing update)
        boolean isTransition = !currentState.equals(nextState) ||
                (currentState == TransactionState.PDNG && nextState == TransactionState.PDNG && callCount == 3);

        // Validate and perform transition
        if (isTransition) {
            nextState = TransactionStateMachine.transition(currentState, nextState);
            updateState(uetr, nextState);

            // Record transition in history
            PaymentTransaction166 response = createResponse(uetr, nextState, callCount);
            String apiData = serializeApiData(response);
            String reason = getTransitionReason(previousState, nextState, callCount);

            TransitionHistory history = new TransitionHistory(
                    uetr, previousState, nextState, apiData, reason);
            historyStore.recordTransition(history);
        }

        return createResponse(uetr, nextState, callCount);
    }

    @Override
    public List<String> getActiveUetrs() {
        return jdbcTemplate.queryForList(
                "SELECT uetr FROM transaction_states WHERE current_state != 'ACCC' AND current_state != 'RJCT'",
                String.class);
    }

    private TransactionState getCurrentState(String uetr) {
        try {
            String stateStr = jdbcTemplate.queryForObject(
                    "SELECT current_state FROM transaction_states WHERE uetr = ?",
                    String.class, uetr);
            return stateStr != null ? TransactionState.valueOf(stateStr) : TransactionState.INIT;
        } catch (Exception e) {
            // First time seeing this UETR, initialize it
            jdbcTemplate.update(
                    "INSERT OR IGNORE INTO transaction_states (uetr, current_state, call_count) VALUES (?, ?, 0)",
                    uetr, TransactionState.INIT.name());
            return TransactionState.INIT;
        }
    }

    private int incrementCallCount(String uetr) {
        jdbcTemplate.update(
                "UPDATE transaction_states SET call_count = call_count + 1 WHERE uetr = ?",
                uetr);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT call_count FROM transaction_states WHERE uetr = ?",
                Integer.class, uetr);
        return count != null ? count : 1;
    }

    private void updateState(String uetr, TransactionState newState) {
        jdbcTemplate.update(
                "UPDATE transaction_states SET current_state = ? WHERE uetr = ?",
                newState.name(), uetr);
    }

    private TransactionState determineNextState(TransactionState current, int callCount) {
        // State transition logic based on call count
        switch (current) {
            case INIT:
                // First call stays in INIT, second call moves to PDNG
                return callCount >= 2 ? TransactionState.PDNG : TransactionState.INIT;

            case PDNG:
                // PDNG can stay in PDNG (adding routes) or move to ACCC
                // Stay in PDNG for calls 2-3, move to ACCC on call 4+
                return callCount >= 4 ? TransactionState.ACCC : TransactionState.PDNG;

            case ACCC:
            case RJCT:
                // Terminal states stay in themselves
                return current;

            default:
                return current;
        }
    }

    private String getTransitionReason(TransactionState from, TransactionState to, int callCount) {
        if (from == TransactionState.INIT && to == TransactionState.PDNG) {
            return "Started processing";
        } else if (from == TransactionState.PDNG && to == TransactionState.PDNG) {
            return "Added extra routing (call " + callCount + ")";
        } else if (from == TransactionState.PDNG && to == TransactionState.ACCC) {
            return "Processing completed successfully";
        } else if (to == TransactionState.RJCT) {
            return "Transaction rejected";
        }
        return "State transition";
    }

    private String serializeApiData(PaymentTransaction166 tx) {
        // Simple serialization - in production use Jackson or similar
        return String.format("{\"uetr\":\"%s\",\"status\":\"%s\",\"description\":\"%s\"}",
                tx.getUETR(), tx.getTransactionStatus(), tx.getTransactionStatusDescription());
    }

    private PaymentTransaction166 createResponse(String uetr, TransactionState state, int callCount) {
        PaymentTransaction166 tx = new PaymentTransaction166();
        tx.setUETR(uetr);
        tx.setTransactionInitiationDateTime("2025-05-23T10:00:40Z");

        List<TransactionRouting1> routing = new ArrayList<>();
        routing.add(new TransactionRouting1().from("BANKBEBICXX").to("BANKUSBICXX"));

        tx.setTransactionInstructedAmount(new PaymentTransaction166TransactionInstructedAmount()
                .currency("USD").amount("100000"));

        // Set response based on state
        switch (state) {
            case INIT:
                tx.setTransactionStatus("INIT");
                tx.setTransactionStatusDescription(state.getDescription());
                tx.setTransactionStatusReason(state.getReason());
                tx.setTransactionLastUpdateDateTime("2025-05-23T10:00:40Z");
                tx.setTransactionRouting(routing);
                break;

            case PDNG:
                tx.setTransactionStatus("PDNG");
                tx.setTransactionStatusDescription(state.getDescription());
                tx.setTransactionStatusReason(state.getReason());
                tx.setTransactionLastUpdateDateTime("2025-05-23T10:02:40Z");

                // Add extra route if this is a PDNG→PDNG transition (call 3)
                if (callCount == 3) {
                    routing.add(new TransactionRouting1().from("BANKUSBICXX").to("BANKFRBICXX"));
                }
                tx.setTransactionRouting(routing);
                break;

            case ACCC:
                tx.setTransactionStatus("ACCC");
                tx.setTransactionStatusDescription(state.getDescription());
                tx.setTransactionStatusReason(state.getReason());
                tx.setTransactionLastUpdateDateTime("2025-05-23T10:05:40Z");
                tx.setTransactionCompletionDateTime("2025-05-23T10:05:40Z");

                // Add the second hop for ACCC
                routing.add(new TransactionRouting1().from("BANKUSBICXX").to("BANKFRBICXX"));
                tx.setTransactionRouting(routing);

                tx.setTransactionConfirmedAmount(new PaymentTransaction166TransactionConfirmedAmount()
                        .currency("USD").amount("99550"));
                break;

            case RJCT:
                tx.setTransactionStatus("RJCT");
                tx.setTransactionStatusDescription(state.getDescription());
                tx.setTransactionStatusReason(state.getReason());
                tx.setTransactionLastUpdateDateTime("2025-05-23T10:02:40Z");
                tx.setTransactionRouting(routing);
                break;
        }

        return tx;
    }
}
