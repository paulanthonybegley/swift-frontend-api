package com.example.cxf.mock;

import com.example.cxf.model.PaymentTransaction166;
import org.junit.Test;
import static org.junit.Assert.*;

public class TrackerApiServiceImplTest {

    private static final String TEST_DB = "test_tracker_api.db";
    private TrackerApiServiceImpl service;

    @org.junit.Before
    public void setUp() {
        // Clean up before test
        new java.io.File(TEST_DB).delete();
        service = new TrackerApiServiceImpl(new SqliteTransactionStateStoreImpl(TEST_DB));
    }

    @org.junit.After
    public void tearDown() {
        // Clean up after test
        new java.io.File(TEST_DB).delete();
    }

    @Test
    public void testGetTransaction_Lifecycle() {
        String uetr = "00f4be35-76f2-45c8-b4b3-565bbac5e86b";

        // 1st call: INIT
        PaymentTransaction166 tx1 = service.getPaymentTransactionInfo(uetr);
        assertNotNull(tx1);
        assertEquals("INIT", tx1.getTransactionStatus());
        assertEquals("Initialized", tx1.getTransactionStatusReason());

        // 2nd call: PDNG (processing start)
        PaymentTransaction166 tx2 = service.getPaymentTransactionInfo(uetr);
        assertEquals("PDNG", tx2.getTransactionStatus());
        assertEquals("Processing", tx2.getTransactionStatusReason());

        // 3rd call: PDNG (extra route added)
        PaymentTransaction166 tx3 = service.getPaymentTransactionInfo(uetr);
        assertEquals("PDNG", tx3.getTransactionStatus());
        assertEquals(2, tx3.getTransactionRouting().size());

        // 4th call: ACCC (completion)
        PaymentTransaction166 tx4 = service.getPaymentTransactionInfo(uetr);
        assertEquals("ACCC", tx4.getTransactionStatus());

        // Verify Transition History directly from DB to confirm insertions
        org.springframework.jdbc.datasource.DriverManagerDataSource ds = new org.springframework.jdbc.datasource.DriverManagerDataSource(
                "jdbc:sqlite:" + TEST_DB);
        org.springframework.jdbc.core.JdbcTemplate jdbc = new org.springframework.jdbc.core.JdbcTemplate(ds);

        java.util.List<java.util.Map<String, Object>> rows = jdbc.queryForList(
                "SELECT from_state, to_state, reason FROM transition_history WHERE uetr = ? ORDER BY id", uetr);

        // We expect 3 recorded transitions:
        // 1. INIT -> PDNG
        // 2. PDNG -> PDNG (extra route)
        // 3. PDNG -> ACCC
        assertEquals(3, rows.size());

        // Check Transition 1: INIT -> PDNG
        assertEquals("INIT", rows.get(0).get("from_state"));
        assertEquals("PDNG", rows.get(0).get("to_state"));
        assertEquals("Started processing", rows.get(0).get("reason"));

        // Check Transition 2: PDNG -> PDNG
        assertEquals("PDNG", rows.get(1).get("from_state"));
        assertEquals("PDNG", rows.get(1).get("to_state"));
        // Reason includes call count "3"
        assertEquals("Added extra routing (call 3)", rows.get(1).get("reason"));

        // Check Transition 3: PDNG -> ACCC
        assertEquals("PDNG", rows.get(2).get("from_state"));
        assertEquals("ACCC", rows.get(2).get("to_state"));
        assertEquals("Processing completed successfully", rows.get(2).get("reason"));
    }
}
