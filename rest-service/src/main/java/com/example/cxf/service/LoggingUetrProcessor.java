package com.example.cxf.service;

import com.example.cxf.model.PaymentTransaction166;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUetrProcessor implements UetrProcessor {
    private static final Logger log = LoggerFactory.getLogger(LoggingUetrProcessor.class);
    private final UetrProcessor delegate;

    public LoggingUetrProcessor(UetrProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public String[] loadUetrs() {
        log.info(">>> Entering loadUetrs()");
        long start = System.currentTimeMillis();
        try {
            String[] uetrs = delegate.loadUetrs();
            long duration = System.currentTimeMillis() - start;
            int count = (uetrs != null) ? uetrs.length : 0;
            log.info("<<< Exiting loadUetrs(): loaded {} UETRs in {}ms", count, duration);
            return uetrs;
        } catch (Exception e) {
            log.error("!!! Error in loadUetrs(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public PaymentTransaction166 getTransaction(String uetr) {
        log.info(">>> Entering getTransaction(uetr='{}')", uetr);
        long start = System.currentTimeMillis();
        try {
            PaymentTransaction166 tx = delegate.getTransaction(uetr);
            long duration = System.currentTimeMillis() - start;
            if (tx != null) {
                log.info("<<< Exiting getTransaction: found transaction with status '{}' [{}ms]", 
                        tx.getTransactionStatus(), duration);
            } else {
                log.warn("<<< Exiting getTransaction: transaction not found [{}ms]", duration);
            }
            return tx;
        } catch (Exception e) {
            log.error("!!! Error in getTransaction for UETR '{}': {}", uetr, e.getMessage(), e);
            throw e;
        }
    }
}
