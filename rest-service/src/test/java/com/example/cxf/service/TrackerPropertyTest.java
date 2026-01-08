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
    private static TrackerService trackerService;
    private static TransactionStateStore store;

    @BeforeAll
    public static void startServer() {
        // Use a test-specific DB file
        java.io.File dbFile = new java.io.File("test_uetrs.db");
        if (dbFile.exists()) dbFile.delete();

        store = StorageFactory.create("test_uetrs.db");

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(TrackerApiServiceImpl.class, InternalUetrController.class);
        sf.setResourceProvider(TrackerApiServiceImpl.class,
                new SingletonResourceProvider(new TrackerApiServiceImpl(store)));
        sf.setResourceProvider(InternalUetrController.class,
                new SingletonResourceProvider(new InternalUetrController(store)));

        sf.setProviders(java.util.Arrays.asList(
                new JacksonJsonProvider(),
                new org.apache.cxf.jaxrs.validation.ValidationExceptionMapper(),
                new BasicAuthFilter()
        ));
        sf.setInInterceptors(Collections.singletonList(new org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor()));
        sf.setAddress(BASE_URL);
        server = sf.create();

        trackerService = new TrackerService(BASE_URL, "admin", "password");
    }

    @Test
    public void testUnauthorizedAccess() {
        TrackerService unauthorizedService = new TrackerService(BASE_URL, "wrong", "wrong");
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
        new java.io.File("test_uetrs.db").delete();
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
        
        // 1. Initial call -> State INIT
        PaymentTransaction166 tx1 = trackerService.getTransaction(uetr);
        assertEquals("INIT", tx1.getTransactionStatus());
        
        // 2. loadUetrs should return this UETR
        String[] active1 = trackerService.loadUetrs();
        assertTrue(java.util.Arrays.asList(active1).contains(uetr));
        
        // 3. Second and third calls -> PDNG
        assertEquals("PDNG", trackerService.getTransaction(uetr).getTransactionStatus());
        assertEquals("PDNG", trackerService.getTransaction(uetr).getTransactionStatus());
        
        // 4. Fourth call -> ACCC
        assertEquals("ACCC", trackerService.getTransaction(uetr).getTransactionStatus());
        
        // 5. loadUetrs should NOT return this UETR anymore
        String[] activeFinal = trackerService.loadUetrs();
        assertFalse(java.util.Arrays.asList(activeFinal).contains(uetr));
    }

    @Test
    public void testPersistenceAcrossRestarts() {
        String uetr = "11111111-2222-4333-8444-555555555555";
        String dbName = "persistence_test.db";
        java.io.File dbFile = new java.io.File(dbName);
        if (dbFile.exists()) dbFile.delete();

        try {
            // 1. First Session
            TransactionStateStore store1 = new SqliteTransactionStateStoreImpl(dbName);
            PaymentTransaction166 tx1 = store1.getOrUpdate(uetr);
            assertEquals("INIT", tx1.getTransactionStatus());

            // 2. Second Session (Simulate Restart)
            TransactionStateStore store2 = new SqliteTransactionStateStoreImpl(dbName);
            PaymentTransaction166 tx2 = store2.getOrUpdate(uetr);
            assertEquals("PDNG", tx2.getTransactionStatus(), "State should be PDNG because it was INIT in previous session");
        } finally {
            if (dbFile.exists()) dbFile.delete();
        }
    }

    @Test
    public void standardTest() {}
}
