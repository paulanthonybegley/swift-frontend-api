package com.example.cxf.service;

import com.example.cxf.api.TrackerFrontEndApi;
import com.example.cxf.model.PaymentTransaction166;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import java.util.Collections;

public class TrackerService {
    private final TrackerFrontEndApi proxy;

    public TrackerService(String baseUrl) {
        this.proxy = JAXRSClientFactory.create(baseUrl, TrackerFrontEndApi.class,
                Collections.singletonList(new JacksonJsonProvider()));
    }

    public PaymentTransaction166 getTransaction(String uetr) {
        try {
            return proxy.getPaymentTransactionInfo(uetr);
        } catch (jakarta.ws.rs.NotFoundException e) {
            return null;
        }
    }
}
