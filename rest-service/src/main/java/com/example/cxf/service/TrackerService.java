package com.example.cxf.service;

import com.example.cxf.api.TrackerFrontEndApi;
import com.example.cxf.model.PaymentTransaction166;
import com.example.cxf.service.persistence.StorageFactory;
import com.example.cxf.service.persistence.TransactionStateStore;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.client.WebClient;
import java.util.Collections;

public class TrackerService implements UetrProcessor {
    private final TrackerFrontEndApi proxy;
    private final TransactionStateStore store;
    private static final String[] DEFAULT_UETRS = {
            "00f4be35-76f2-45c8-b4b3-565bbac5e86b",
            "eec67498-8422-4a00-9844-325d7e305e75"
    };

    public TrackerService(String baseUrl) {
        this(baseUrl, null, null);
    }

    public TrackerService(String baseUrl, String username, String password) {
        this(baseUrl, username, password, StorageFactory.create());
    }

    public TrackerService(String baseUrl, String username, String password, TransactionStateStore store) {
        this.proxy = JAXRSClientFactory.create(baseUrl, TrackerFrontEndApi.class,
                Collections.singletonList(new JacksonJsonProvider()));
        this.store = store;

        // Seed with default demonstration UETRs (always add to store, but won't affect
        // discovery if already ACCC)
        for (String uetr : DEFAULT_UETRS) {
            this.store.addUetr(uetr);
        }

        if (username != null && password != null) {
            AuthorizationPolicy authPolicy = new AuthorizationPolicy();
            authPolicy.setUserName(username);
            authPolicy.setPassword(password);
            authPolicy.setAuthorizationType("Basic");

            WebClient.getConfig(this.proxy).getHttpConduit().setAuthorization(authPolicy);
        }
    }

    @Override
    public String[] loadUetrs() {
        String[] allUetrs = store.getAllUetrs().toArray(new String[0]);
        String[] activeUetrs = store.getActiveUetrs().toArray(new String[0]);

        System.out.println("allUetrs: " + allUetrs.length);
        System.out.println("activeUetrs: " + activeUetrs.length);

        // Configurable fallback: seed defaults if empty
        boolean seedEnabled = Boolean.parseBoolean(System.getProperty("service.seed.enabled", "true"));
        if (seedEnabled && allUetrs.length == 0) {
            int seedCount = Integer.parseInt(System.getProperty("service.seed.count", "2"));
            System.out.println("No active UETRs found. Seeding " + seedCount
                    + " default UETRs (service.seed.enabled=true) and all is " + allUetrs.length + " active is "
                    + activeUetrs.length);

            // Reset status for the first N default UETRs to make them active again
            for (int i = 0; i < Math.min(seedCount, DEFAULT_UETRS.length); i++) {
                store.updateStatus(DEFAULT_UETRS[i], null); // null status = active
            }

            activeUetrs = store.getActiveUetrs().toArray(new String[0]);
        }

        return activeUetrs;
    }

    public void setUetrs(java.util.List<String> uetrs) {
        if (uetrs != null) {
            for (String uetr : uetrs) {
                store.addUetr(uetr);
            }
        }
    }

    @Override
    public PaymentTransaction166 getTransaction(String uetr) {
        try {
            PaymentTransaction166 tx = proxy.getPaymentTransactionInfo(uetr);
            if (tx != null) {
                store.updateStatus(uetr, tx.getTransactionStatus());
            }
            return tx;
        } catch (jakarta.ws.rs.NotFoundException e) {
            return null;
        } catch (jakarta.ws.rs.WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            // Log or handle network error
            return null;
        }
    }
}
