package com.example.cxf.service.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;

public class SqliteTransactionStateStoreImpl implements TransactionStateStore {
    private final JdbcTemplate jdbcTemplate;

    public SqliteTransactionStateStoreImpl(String dbPath) {
        DataSource dataSource = new DriverManagerDataSource("jdbc:sqlite:" + dbPath);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        initDb();
    }

    private void initDb() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS service_transaction_states (" +
                "uetr TEXT PRIMARY KEY, " +
                "status TEXT)");
    }

    @Override
    public void updateStatus(String uetr, String status) {
        jdbcTemplate.update("INSERT INTO service_transaction_states (uetr, status) VALUES (?, ?) " +
                "ON CONFLICT(uetr) DO UPDATE SET status = ?", uetr, status, status);
    }

    @Override
    public String getStatus(String uetr) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT status FROM service_transaction_states WHERE uetr = ?",
                    String.class, uetr);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> getActiveUetrs() {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT uetr FROM service_transaction_states WHERE status IS NULL OR status != 'ACCC'",
                    String.class);
        } catch (Exception e) {
            // Database might have been deleted or connection lost - reinitialize
            initDb();
            return jdbcTemplate.queryForList(
                    "SELECT uetr FROM service_transaction_states WHERE status IS NULL OR status != 'ACCC'",
                    String.class);
        }
    }

    public List<String> getAllUetrs() {
        return jdbcTemplate.queryForList(
                "SELECT uetr FROM service_transaction_states", String.class);
    }

    @Override
    public void addUetr(String uetr) {
        jdbcTemplate.update("INSERT OR IGNORE INTO service_transaction_states (uetr) VALUES (?)", uetr);
    }
}
