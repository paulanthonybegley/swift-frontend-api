package com.example.cxf.service.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryTransactionStateStoreImpl implements TransactionStateStore {
    private final Map<String, String> store = new ConcurrentHashMap<>();

    @Override
    public void updateStatus(String uetr, String status) {
        store.put(uetr, status);
    }

    @Override
    public String getStatus(String uetr) {
        return store.get(uetr);
    }

    @Override
    public List<String> getActiveUetrs() {
        return store.entrySet().stream()
                .filter(entry -> entry.getValue() == null || !"ACCC".equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllUetrs() {
        return store.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void addUetr(String uetr) {
        store.putIfAbsent(uetr, ""); // Empty status means active but unknown
    }
}
