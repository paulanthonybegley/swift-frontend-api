package com.example.cxf.service.persistence;

import java.util.List;

public interface TransactionStateStore {
    /**
     * Updates or inserts the last known status for a UETR.
     */
    void updateStatus(String uetr, String status);

    /**
     * Gets the last known status for a UETR.
     */
    String getStatus(String uetr);

    /**
     * Returns a list of UETRs that are considered active (status != 'ACCC').
     */
    List<String> getActiveUetrs();

    List<String> getAllUetrs(); // New method

    /**
     * Adds a UETR to the tracking list if not already present.
     */
    void addUetr(String uetr);
}
