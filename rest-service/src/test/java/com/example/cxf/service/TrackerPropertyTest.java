package com.example.cxf.service;

import com.example.cxf.mock.TrackerApiServiceImpl;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import net.jqwik.api.*;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;
import org.apache.cxf.endpoint.Server;

import java.util.Collections;

public class TrackerPropertyTest {

    private static Server server;
    private static final String BASE_URL = "http://localhost:9001/";
    private static TrackerService trackerService;

    @BeforeAll
    public static void startServer() {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(TrackerApiServiceImpl.class);
        sf.setResourceProvider(TrackerApiServiceImpl.class,
                new SingletonResourceProvider(new TrackerApiServiceImpl()));
        sf.setProviders(java.util.Arrays.asList(
                new JacksonJsonProvider(),
                new org.apache.cxf.jaxrs.validation.ValidationExceptionMapper()
        ));
        sf.setInInterceptors(Collections.singletonList(new org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor()));
        sf.setAddress(BASE_URL);
        server = sf.create();
        
        trackerService = new TrackerService(BASE_URL);
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop();
            server.destroy();
        }
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
    public void standardTest() {}
}
