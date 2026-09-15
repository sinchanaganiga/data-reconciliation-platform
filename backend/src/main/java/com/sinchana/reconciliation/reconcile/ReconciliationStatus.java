package com.sinchana.reconciliation.reconcile;

public enum ReconciliationStatus {
    MATCHED,
    CHANGED,
    MISSING,
    EXTRA,
    DUPLICATE,
    INVALID_KEY
}
