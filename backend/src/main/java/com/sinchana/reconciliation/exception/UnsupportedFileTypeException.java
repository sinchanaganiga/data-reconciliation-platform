package com.sinchana.reconciliation.exception;

public class UnsupportedFileTypeException extends ReconciliationException {
    public UnsupportedFileTypeException(String message) {
        super("UNSUPPORTED_FILE_TYPE", message);
    }
}
