package com.example.cxf.service;

import com.example.cxf.model.PaymentTransaction166;
import com.example.cxf.mock.*;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import net.jqwik.api.*;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.cxf.endpoint.Server;

import jakarta.ws.rs.core.Response;
import java.util.Collections;

public class TrackerPropertyTest {

    private static Server server;
    private static final String BASE_URL = "http://localhost:9001/";
    private static UetrProcessor trackerService;
    private static com.example.cxf.mock.TransactionStateStore mockStore;

    @BeforeAll
    public static void startServer() {
        // Use test-specific DB files
        java.io.File mockDb = new java.io.File("test_mock_uetrs.db");
        if (mockDb.exists()) mockDb.delete();
        java.io.File serviceDb = new java.io.File("test_service_uetrs.db");
        if (serviceDb.exists()) serviceDb.delete();

        mockStore = com.example.cxf.mock.StorageFactory.create("test_mock_uetrs.db");

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(TrackerApiServiceImpl.class);
        sf.setResourceProvider(TrackerApiServiceImpl.class,
                new SingletonResourceProvider(new TrackerApiServiceImpl(mockStore)));

        sf.setProviders(java.util.Arrays.asList(
                new JacksonJsonProvider(),
                new org.apache.cxf.jaxrs.validation.ValidationExceptionMapper(),
                new BasicAuthFilter()
        ));
        sf.setInInterceptors(Collections.singletonList(new org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor()));
        sf.setAddress(BASE_URL);
        server = sf.create();

        com.example.cxf.service.persistence.TransactionStateStore serviceStore = 
                com.example.cxf.service.persistence.StorageFactory.create("test_service_uetrs.db");
        trackerService = new LoggingUetrProcessor(new TrackerService(BASE_URL, "admin", "password", serviceStore));
    }

    @Test
    public void testUnauthorizedAccess() {
        UetrProcessor unauthorizedService = new LoggingUetrProcessor(new TrackerService(BASE_URL, "wrong", "wrong"));
        try {
            unauthorizedService.getTransaction("00f4be35-76f2-45c8-b4b3-565bbac5e86b");
            fail("Should have thrown NotAuthorizedException");
        } catch (jakarta.ws.rs.NotAuthorizedException e) {
            // Success: 401 received
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), e.getResponse().getStatus());
        }
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop();
            server.destroy();
        }
        new java.io.File("test_mock_uetrs.db").delete();
        new java.io.File("test_service_uetrs.db").delete();
    }

    @Property
    public void fuzzGetTransaction(@ForAll String uetr) {
        try {
            trackerService.getTransaction(uetr);
        } catch (jakarta.ws.rs.BadRequestException e) {
            // Success: Validation caught the invalid UETR
            System.out.println("Validation caught invalid UETR: " + uetr);
        } catch (Exception e) {
            // Other exceptions might occur depending on simulated logic
        }
    }

    @Test
    public void testInvalidUetrPattern() {
        try {
            trackerService.getTransaction("invalid-uuid-pattern");
            fail("Should have thrown BadRequestException");
        } catch (jakarta.ws.rs.BadRequestException e) {
            // Success
        }
    }

    @Test
    public void testLifecycleTransitions() {
        String uetr = "00f4be35-76f2-45c8-b4b3-565bbac5e86b";
        
        // 1. Initially, it should be in the loadUetrs list
        assertTrue(java.util.Arrays.asList(trackerService.loadUetrs()).contains(uetr), "UETR should be in discovery list initially");

        // 2. Transition to ACCC
        trackerService.getTransaction(uetr); // INIT
        trackerService.getTransaction(uetr); // PDNG
        trackerService.getTransaction(uetr); // PDNG
        PaymentTransaction166 finalTx = trackerService.getTransaction(uetr); // ACCC
        assertEquals("ACCC", finalTx.getTransactionStatus());
        
        // 3. Now it should be filtered out from loadUetrs
        assertFalse(java.util.Arrays.asList(trackerService.loadUetrs()).contains(uetr), "UETR should be filtered out after reaching ACCC");
    }

    @Test
    public void testMockPersistenceAcrossRestarts() {
        String uetr = "11111111-2222-4333-8444-555555555555";
        String dbName = "mock_persistence_test.db";
        java.io.File dbFile = new java.io.File(dbName);
        if (dbFile.exists()) dbFile.delete();

        try {
            // 1. First Session
            com.example.cxf.mock.TransactionStateStore store1 = new com.example.cxf.mock.SqliteTransactionStateStoreImpl(dbName);
            PaymentTransaction166 tx1 = store1.getOrUpdate(uetr);
            assertEquals("INIT", tx1.getTransactionStatus());

            // 2. Second Session (Simulate Restart)
            com.example.cxf.mock.TransactionStateStore store2 = new com.example.cxf.mock.SqliteTransactionStateStoreImpl(dbName);
            PaymentTransaction166 tx2 = store2.getOrUpdate(uetr);
            assertEquals("PDNG", tx2.getTransactionStatus(), "State should be PDNG because it was INIT in previous session");
        } finally {
            if (dbFile.exists()) dbFile.delete();
        }
    }

    @Test
    public void testServicePersistenceAcrossRestarts() {
        String uetr = "22222222-3333-4444-8444-555555555555";
        String dbName = "service_persistence_test.db";
        java.io.File dbFile = new java.io.File(dbName);
        if (dbFile.exists()) dbFile.delete();

        try {
            // 1. First Session: Service tracks a status
            com.example.cxf.service.persistence.TransactionStateStore store1 = 
                    new com.example.cxf.service.persistence.SqliteTransactionStateStoreImpl(dbName);
            store1.updateStatus(uetr, "INIT");
            assertTrue(store1.getActiveUetrs().contains(uetr), "Should be active when INIT");

            // 2. Second Session: Service remembers the status
            com.example.cxf.service.persistence.TransactionStateStore store2 = 
                    new com.example.cxf.service.persistence.SqliteTransactionStateStoreImpl(dbName);
            assertEquals("INIT", store2.getStatus(uetr));
            
            // 3. Mark as ACCC
            store2.updateStatus(uetr, "ACCC");
            assertFalse(store2.getActiveUetrs().contains(uetr), "Should not be active when ACCC");
        } finally {
            if (dbFile.exists()) dbFile.delete();
        }
    }

    @Test
    public void standardTest() {}
}
