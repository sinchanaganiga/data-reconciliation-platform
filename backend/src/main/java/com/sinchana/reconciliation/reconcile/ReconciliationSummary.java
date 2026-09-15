package com.sinchana.reconciliation.reconcile;

import java.util.List;

public record ReconciliationSummary(
        long total,
        long matched,
        long changed,
        long missing,
        long extra,
        long duplicate,
        long invalidKey) {

    public static ReconciliationSummary from(List<RecordReconciliation> records) {
        return new ReconciliationSummary(
                records.size(),
                count(records, ReconciliationStatus.MATCHED),
                count(records, ReconciliationStatus.CHANGED),
                count(records, ReconciliationStatus.MISSING),
                count(records, ReconciliationStatus.EXTRA),
                count(records, ReconciliationStatus.DUPLICATE),
                count(records, ReconciliationStatus.INVALID_KEY));
    }

    private static long count(List<RecordReconciliation> records, ReconciliationStatus status) {
        return records.stream().filter(record -> record.status() == status).count();
    }
}
