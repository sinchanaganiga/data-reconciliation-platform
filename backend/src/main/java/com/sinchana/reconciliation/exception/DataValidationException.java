package com.sinchana.reconciliation.exception;

public class DataValidationException extends ReconciliationException {
    public DataValidationException(String message) { super("DATA_VALIDATION_ERROR", message); }
}
