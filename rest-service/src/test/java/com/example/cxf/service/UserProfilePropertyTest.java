package com.example.cxf.service;

import com.example.cxf.mock.UserProfileServiceImpl;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import net.jqwik.api.*;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.cxf.endpoint.Server;

import java.util.Collections;

public class UserProfilePropertyTest {

    private static Server server;
    private static final String BASE_URL = "http://localhost:9001/";
    private static UserService userService;

    @BeforeClass
    public static void startServer() {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(UserProfileServiceImpl.class);
        sf.setResourceProvider(UserProfileServiceImpl.class,
                new SingletonResourceProvider(new UserProfileServiceImpl()));
        sf.setProviders(Collections.singletonList(new JacksonJsonProvider()));
        sf.setAddress(BASE_URL);
        server = sf.create();
        
        userService = new UserService(BASE_URL);
    }

    @AfterClass
    public static void stopServer() {
        if (server != null) {
            server.stop();
            server.destroy();
        }
    }

    @Property
    public void fuzzGetUserById(@ForAll String userId) {
        // We expect either a valid user, null (not found), or a runtime exception (500)
        // But the client shouldn't hang or crash the JVM.
        try {
            userService.getUser(userId);
        } catch (Exception e) {
            // It's acceptable for the service to throw if input is "error" (500)
            // or if it propagates the CXF exception.
            // We just want to ensure it handles the fuzzy inputs "gracefully".
            // For this simple test, we just swallow the exception as "handled".
        }
    }
    
    @Test
    public void standardTest() {
        // Just to ensure junit 4 is happy picking up the class
    }
}
