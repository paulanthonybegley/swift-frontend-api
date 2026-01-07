package com.example.cxf.mock;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

import java.util.Collections;

public class App {
    public static void main(String[] args) throws Exception {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(UserProfileServiceImpl.class);
        sf.setResourceProvider(UserProfileServiceImpl.class,
                new SingletonResourceProvider(new UserProfileServiceImpl()));
        sf.setProviders(Collections.singletonList(new JacksonJsonProvider()));
        sf.setAddress("http://localhost:9000/");

        sf.create();
        System.out.println("Server ready at http://localhost:9000/");
        Thread.sleep(Long.MAX_VALUE); // Keep running
    }
}
