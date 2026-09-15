package com.sinchana.reconciliation.reconcile;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

public record RecordReconciliation(
        ReconciliationStatus status,
        Object key,
        Integer sourceRowIndex,
        Integer targetRowIndex,
        Map<String, Object> sourceRecord,
        Map<String, Object> targetRecord,
        List<Integer> sourceRowIndices,
        List<Integer> targetRowIndices,
        List<Map<String, Object>> sourceRecords,
        List<Map<String, Object>> targetRecords,
        List<FieldDifference> differences) {

    public RecordReconciliation {
        sourceRowIndices = List.copyOf(sourceRowIndices);
        targetRowIndices = List.copyOf(targetRowIndices);
        sourceRecords = sourceRecords.stream()
                .map(values -> Collections.unmodifiableMap(new LinkedHashMap<>(values)))
                .toList();
        targetRecords = targetRecords.stream()
                .map(values -> Collections.unmodifiableMap(new LinkedHashMap<>(values)))
                .toList();
        differences = List.copyOf(differences);
    }
}
