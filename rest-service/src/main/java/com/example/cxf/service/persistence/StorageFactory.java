package com.example.cxf.service.persistence;

public class StorageFactory {
    public static TransactionStateStore create() {
        String storageType = System.getProperty("service.storage", "sqlite");
        System.out.println("Initializing service storage using mode: " + storageType);

        if ("sqlite".equalsIgnoreCase(storageType)) {
            return new SqliteTransactionStateStoreImpl("service_uetrs.db");
        } else {
            return new InMemoryTransactionStateStoreImpl();
        }
    }

    public static TransactionStateStore create(String dbName) {
        String storageType = System.getProperty("service.storage", "sqlite");
        if ("sqlite".equalsIgnoreCase(storageType)) {
            return new SqliteTransactionStateStoreImpl(dbName);
        } else {
            return new InMemoryTransactionStateStoreImpl();
        }
    }
}
