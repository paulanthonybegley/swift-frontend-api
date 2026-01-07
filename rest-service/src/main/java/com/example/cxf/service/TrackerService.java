package com.example.cxf.service;

import com.example.cxf.api.TrackerFrontEndApi;
import com.example.cxf.model.PaymentTransaction166;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import java.util.Collections;

public class TrackerService implements UetrProcessor {
    private final TrackerFrontEndApi proxy;
    private final InternalTrackerApi internalProxy;

    public TrackerService(String baseUrl) {
        this.proxy = JAXRSClientFactory.create(baseUrl, TrackerFrontEndApi.class,
                Collections.singletonList(new JacksonJsonProvider()));
        this.internalProxy = JAXRSClientFactory.create(baseUrl, InternalTrackerApi.class,
                Collections.singletonList(new JacksonJsonProvider()));
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
