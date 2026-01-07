package com.example.cxf.service;

import com.example.cxf.mock.TrackerApiServiceImpl;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import net.jqwik.api.*;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.cxf.endpoint.Server;

import java.util.Collections;

public class TrackerPropertyTest {

    private static Server server;
    private static final String BASE_URL = "http://localhost:9001/";
    private static TrackerService trackerService;

    @BeforeClass
    public static void startServer() {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(TrackerApiServiceImpl.class);
        sf.setResourceProvider(TrackerApiServiceImpl.class,
                new SingletonResourceProvider(new TrackerApiServiceImpl()));
        sf.setProviders(Collections.singletonList(new JacksonJsonProvider()));
        sf.setAddress(BASE_URL);
        server = sf.create();
        
        trackerService = new TrackerService(BASE_URL);
    }

    @AfterClass
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
        } catch (Exception e) {
            // Expected for fuzzy inputs
        }
    }

    @Test
    public void standardTest() {}
}
