package com.example.cxf.mock;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;

import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;

public class App {
    public static void main(String[] args) throws Exception {
        TransactionStateStore store = StorageFactory.create();

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(TrackerApiServiceImpl.class);

        sf.setResourceProvider(TrackerApiServiceImpl.class,
                new SingletonResourceProvider(new TrackerApiServiceImpl(store)));

        sf.setProviders(java.util.Arrays.asList(new JacksonJsonProvider(), new BasicAuthFilter()));
        sf.setInInterceptors(java.util.Arrays.asList(new JAXRSBeanValidationInInterceptor()));
        sf.setAddress("http://localhost:9000/");

        sf.create();
        System.out.println("Tracker Mock Server ready at http://localhost:9000/");
        Thread.sleep(Long.MAX_VALUE);
    }
}
