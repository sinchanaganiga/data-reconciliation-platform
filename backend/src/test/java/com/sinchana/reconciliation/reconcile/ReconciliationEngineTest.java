package com.sinchana.reconciliation.reconcile;

import com.sinchana.reconciliation.key.ReconciliationConfiguration;
import com.sinchana.reconciliation.model.DataSet;
import com.sinchana.reconciliation.normalize.ComparisonConfiguration;
import com.sinchana.reconciliation.normalize.DataNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReconciliationEngineTest {
    private final ReconciliationEngine engine = new ReconciliationEngine(new DataNormalizer(), new RecordMatcher());

    @Test
    void reportsAllReconciliationOutcomesAndOriginalDifferences() {
        var source = new DataSet("source", List.of("source_id", "value"), List.of(
                Map.of("source_id", "same", "value", "A"),
                Map.of("source_id", "changed", "value", "A"),
                Map.of("source_id", "missing", "value", "A"),
                Map.of("source_id", "duplicate", "value", "A"),
                Map.of("source_id", "duplicate", "value", "B")));
        var target = new DataSet("target", List.of("target_id", "value"), List.of(
                Map.of("target_id", "same", "value", "A"),
                Map.of("target_id", "changed", "value", "B"),
                Map.of("target_id", "extra", "value", "A"),
                Map.of("target_id", "duplicate", "value", "A")));

        var result = engine.reconcile(source, target,
                new ReconciliationConfiguration("source_id", "target_id", ComparisonConfiguration.defaults()));

        assertEquals(1, result.matchedCount());
        assertEquals(1, result.changedCount());
        assertEquals(1, result.missingCount());
        assertEquals(1, result.extraCount());
        assertEquals(1, result.duplicateCount());
        assertEquals(new ReconciliationSummary(5, 1, 1, 1, 1, 1, 0), result.summary());
        var duplicate = result.records().stream()
                .filter(record -> record.status() == ReconciliationStatus.DUPLICATE)
                .findFirst().orElseThrow();
        assertEquals(List.of(3, 4), duplicate.sourceRowIndices());
        assertEquals(List.of(3), duplicate.targetRowIndices());
        var changed = result.records().stream()
                .filter(record -> record.status() == ReconciliationStatus.CHANGED)
                .findFirst().orElseThrow();
        assertEquals("A", changed.differences().get(0).sourceValue());
        assertEquals("B", changed.differences().get(0).targetValue());
    }

    @Test
    void reportsInvalidKeyForRowsWithoutKeysInsteadOfMatchingThem() {
        var source = new DataSet("source", List.of("source_id", "value"),
                List.of(java.util.Collections.singletonMap("source_id", null),
                        Map.of("source_id", " ")));
        var target = new DataSet("target", List.of("target_id", "value"),
                List.of(java.util.Collections.singletonMap("target_id", null),
                        Map.of("target_id", " ")));

        var result = engine.reconcile(source, target,
                new ReconciliationConfiguration("source_id", "target_id", ComparisonConfiguration.defaults()));

        assertEquals(0, result.matchedCount());
        assertEquals(0, result.missingCount());
        assertEquals(0, result.extraCount());
        assertEquals(0, result.duplicateCount());
        assertEquals(4, result.summary().total());
        assertEquals(4, result.summary().invalidKey());
        assertEquals(new ReconciliationSummary(4, 0, 0, 0, 0, 0, 4), result.summary());
        assertEquals(4, result.records().stream()
                .filter(record -> record.status() == ReconciliationStatus.INVALID_KEY)
                .count());
        assertEquals(List.of(0), result.records().get(0).sourceRowIndices());
        assertEquals(List.of(1), result.records().get(1).sourceRowIndices());
        assertEquals(List.of(0), result.records().get(2).targetRowIndices());
        assertEquals(List.of(1), result.records().get(3).targetRowIndices());
        assertEquals(0, result.records().get(0).targetRecords().size());
        assertEquals(0, result.records().get(2).sourceRecords().size());
    }

    @Test
    void producesAnEmptySummaryForEmptyDatasets() {
        var empty = new DataSet("empty", List.of("id"), List.of());

        var result = engine.reconcile(empty, empty,
                new ReconciliationConfiguration("id", "id", ComparisonConfiguration.defaults()));

        assertEquals(new ReconciliationSummary(0, 0, 0, 0, 0, 0, 0), result.summary());
        assertEquals(List.of(), result.records());
    }
}
