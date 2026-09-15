package com.sinchana.reconciliation.exception;

public class ReconciliationException extends RuntimeException {
    private final String code;

    public ReconciliationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ReconciliationException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String code() { return code; }
}
