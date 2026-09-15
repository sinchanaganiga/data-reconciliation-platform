package com.sinchana.reconciliation.exception;

public class DataReadException extends ReconciliationException {
    public DataReadException(String message) { super("DATA_READ_ERROR", message); }
    public DataReadException(String message, Throwable cause) { super("DATA_READ_ERROR", message, cause); }
}
