package com.sinchana.reconciliation.reconcile;

import java.time.Instant;

public record ReconciliationMetadata(
        String runId,
        String sourceFileName,
        String targetFileName,
        String sourceKey,
        String targetKey,
        Instant executedAt) {

    public static ReconciliationMetadata empty() {
        return new ReconciliationMetadata(null, null, null, null, null, null);
    }
}
