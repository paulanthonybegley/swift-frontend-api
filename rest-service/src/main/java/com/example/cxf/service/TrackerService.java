package com.example.cxf.service;

import com.example.cxf.api.TrackerFrontEndApi;
import com.example.cxf.model.PaymentTransaction166;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.client.WebClient;
import java.util.Collections;

public class TrackerService implements UetrProcessor {
    private final TrackerFrontEndApi proxy;
    private final InternalTrackerApi internalProxy;

    public TrackerService(String baseUrl) {
        this(baseUrl, null, null);
    }

    public TrackerService(String baseUrl, String username, String password) {
        this.proxy = JAXRSClientFactory.create(baseUrl, TrackerFrontEndApi.class,
                Collections.singletonList(new JacksonJsonProvider()));
        this.internalProxy = JAXRSClientFactory.create(baseUrl, InternalTrackerApi.class,
                Collections.singletonList(new JacksonJsonProvider()));

        if (username != null && password != null) {
            AuthorizationPolicy authPolicy = new AuthorizationPolicy();
            authPolicy.setUserName(username);
            authPolicy.setPassword(password);
            authPolicy.setAuthorizationType("Basic");

            WebClient.getConfig(this.proxy).getHttpConduit().setAuthorization(authPolicy);
            WebClient.getConfig(this.internalProxy).getHttpConduit().setAuthorization(authPolicy);
        }
    }

    @Override
    public String[] loadUetrs() {
        try {
            java.util.List<String> activeUetrs = internalProxy.getActiveUetrs();
            return activeUetrs.toArray(new String[0]);
        } catch (Exception e) {
            return new String[0];
        }
    }

    @Override
    public PaymentTransaction166 getTransaction(String uetr) {
        try {
            return proxy.getPaymentTransactionInfo(uetr);
        } catch (jakarta.ws.rs.NotFoundException e) {
            return null;
        }
    }
}
