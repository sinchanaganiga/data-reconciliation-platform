package com.sinchana.reconciliation.exception;

public class ReportNotFoundException extends ReconciliationException {
    public ReportNotFoundException(String runId) {
        super("REPORT_NOT_FOUND", "The reconciliation result is no longer available: " + runId);
    }
}
