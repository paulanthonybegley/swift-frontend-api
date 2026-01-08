package com.example.cxf.mock;

public class StorageFactory {
    public static TransactionStateStore create() {
        String storageType = System.getProperty("mock.storage", "sqlite");
        System.out.println("Initializing storage using mode: " + storageType);

        if ("sqlite".equalsIgnoreCase(storageType)) {
            return new SqliteTransactionStateStoreImpl("uetrs.db");
        } else {
            return new TransactionStateStoreImpl();
        }
    }

    public static TransactionStateStore create(String dbName) {
        String storageType = System.getProperty("mock.storage", "sqlite");
        if ("sqlite".equalsIgnoreCase(storageType)) {
            return new SqliteTransactionStateStoreImpl(dbName);
        } else {
            return new TransactionStateStoreImpl();
        }
    }
}
