package com.example.cxf.mock;

import com.example.cxf.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class SqliteTransactionStateStoreImpl implements TransactionStateStore {
    private final JdbcTemplate jdbcTemplate;

    public SqliteTransactionStateStoreImpl(String dbPath) {
        DataSource dataSource = new DriverManagerDataSource("jdbc:sqlite:" + dbPath);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        initDb();
    }

    private void initDb() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transaction_states (" +
                "uetr TEXT PRIMARY KEY, " +
                "call_count INTEGER DEFAULT 0)");
    }

    @Override
    public PaymentTransaction166 getOrUpdate(String uetr) {
        // UPSERT
        jdbcTemplate.update("INSERT INTO transaction_states (uetr, call_count) VALUES (?, 1) " +
                "ON CONFLICT(uetr) DO UPDATE SET call_count = call_count + 1", uetr);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT call_count FROM transaction_states WHERE uetr = ?",
                Integer.class, uetr);

        return createResponse(uetr, count != null ? count : 1);
    }

    @Override
    public List<String> getActiveUetrs() {
        return jdbcTemplate.queryForList(
                "SELECT uetr FROM transaction_states WHERE call_count < 4",
                String.class);
    }

    private PaymentTransaction166 createResponse(String uetr, int currentCall) {
        PaymentTransaction166 tx = new PaymentTransaction166();
        tx.setUETR(uetr);
        tx.setTransactionInitiationDateTime("2025-05-23T10:00:40Z");

        List<TransactionRouting1> routing = new ArrayList<>();
        routing.add(new TransactionRouting1().from("BANKBEBICXX").to("BANKUSBICXX"));

        tx.setTransactionInstructedAmount(new PaymentTransaction166TransactionInstructedAmount()
                .currency("USD").amount("100000"));

        if (currentCall == 1) {
            tx.setTransactionStatus("INIT");
            tx.setTransactionStatusDescription("Initial Status");
            tx.setTransactionStatusReason("Initialized");
            tx.setTransactionLastUpdateDateTime("2025-05-23T10:00:40Z");
            tx.setTransactionRouting(routing);
        } else if (currentCall <= 3) {
            tx.setTransactionStatus("PDNG");
            tx.setTransactionStatusDescription("Payment Pending");
            tx.setTransactionStatusReason("Processing");
            tx.setTransactionLastUpdateDateTime("2025-05-23T10:02:40Z");
            tx.setTransactionRouting(routing);
        } else {
            tx.setTransactionStatus("ACCC");
            tx.setTransactionStatusDescription("Payment credited to beneficiary bank");
            tx.setTransactionStatusReason("Credited");
            tx.setTransactionLastUpdateDateTime("2025-05-23T10:05:40Z");
            tx.setTransactionCompletionDateTime("2025-05-23T10:05:40Z");

            // Add the second hop for ACCC
            routing.add(new TransactionRouting1().from("BANKUSBICXX").to("BANKFRBICXX"));
            tx.setTransactionRouting(routing);

            tx.setTransactionConfirmedAmount(new PaymentTransaction166TransactionConfirmedAmount()
                    .currency("USD").amount("99550"));
        }
        return tx;
    }
}
