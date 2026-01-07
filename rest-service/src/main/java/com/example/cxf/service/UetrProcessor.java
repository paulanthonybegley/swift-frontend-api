package com.example.cxf.service;

import com.example.cxf.model.PaymentTransaction166;

public interface UetrProcessor {
    String[] loadUetrs();
    PaymentTransaction166 getTransaction(String uetr);
}
