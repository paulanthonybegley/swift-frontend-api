package com.example.cxf.mock;

import com.example.cxf.model.TransitionHistory;
import com.example.cxf.model.TransactionState;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;

/**
 * Stores transition history in SQLite database for audit trail.
 * Records every state transition with timestamp and API data.
 */
public class TransitionHistoryStore {
    private final JdbcTemplate jdbcTemplate;

    public TransitionHistoryStore(String dbPath) {
        DataSource dataSource = new DriverManagerDataSource("jdbc:sqlite:" + dbPath);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        initDb();
    }

    private void initDb() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transition_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uetr TEXT NOT NULL, " +
                "from_state TEXT NOT NULL, " +
                "to_state TEXT NOT NULL, " +
                "timestamp TEXT NOT NULL, " +
                "api_data TEXT, " +
                "reason TEXT)");

        // Create index for efficient UETR lookups
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_uetr ON transition_history(uetr)");
    }

    /**
     * Records a state transition in the history table.
     */
    public void recordTransition(TransitionHistory history) {
        jdbcTemplate.update(
                "INSERT INTO transition_history (uetr, from_state, to_state, timestamp, api_data, reason) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                history.getUetr(),
                history.getFromState().name(),
                history.getToState().name(),
                history.getTimestamp().toString(),
                history.getApiData(),
                history.getReason());
    }

    /**
     * Gets all transitions for a specific UETR, ordered by timestamp.
     */
    public List<TransitionHistory> getTransitionHistory(String uetr) {
        return jdbcTemplate.query(
                "SELECT uetr, from_state, to_state, timestamp, api_data, reason " +
                        "FROM transition_history WHERE uetr = ? ORDER BY timestamp ASC",
                (rs, rowNum) -> {
                    String fromStateStr = rs.getString("from_state");
                    String toStateStr = rs.getString("to_state");

                    return new TransitionHistory(
                            rs.getString("uetr"),
                            fromStateStr != null ? TransactionState.valueOf(fromStateStr) : null,
                            toStateStr != null ? TransactionState.valueOf(toStateStr) : null,
                            rs.getString("api_data"),
                            rs.getString("reason"));
                },
                uetr);
    }

    /**
     * Gets the count of transitions for a specific UETR.
     */
    public int getTransitionCount(String uetr) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transition_history WHERE uetr = ?",
                Integer.class,
                uetr);
        return count != null ? count : 0;
    }

    /**
     * Gets all UETRs that have transition history.
     */
    public List<String> getAllTrackedUetrs() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT uetr FROM transition_history ORDER BY uetr",
                String.class);
    }
}
