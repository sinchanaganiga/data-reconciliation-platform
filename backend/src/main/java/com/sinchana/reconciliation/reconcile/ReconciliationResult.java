package com.sinchana.reconciliation.reconcile;

import java.util.List;

public record ReconciliationResult(
        List<RecordReconciliation> records,
        ReconciliationSummary summary,
        ReconciliationMetadata metadata) {

    public ReconciliationResult(List<RecordReconciliation> records) {
        this(records, ReconciliationMetadata.empty());
    }

    public ReconciliationResult(List<RecordReconciliation> records, ReconciliationMetadata metadata) {
        this(records, null, metadata);
    }

    public ReconciliationResult {
        records = List.copyOf(records);
        summary = ReconciliationSummary.from(records);
        metadata = metadata == null ? ReconciliationMetadata.empty() : metadata;
    }

    public long matchedCount() { return summary.matched(); }
    public long changedCount() { return summary.changed(); }
    public long missingCount() { return summary.missing(); }
    public long extraCount() { return summary.extra(); }
    public long duplicateCount() { return summary.duplicate(); }
}
