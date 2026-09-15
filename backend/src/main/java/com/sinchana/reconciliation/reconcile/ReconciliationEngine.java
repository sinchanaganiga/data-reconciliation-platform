package com.sinchana.reconciliation.reconcile;

import com.sinchana.reconciliation.key.ReconciliationConfiguration;
import com.sinchana.reconciliation.normalize.DataNormalizer;
import com.sinchana.reconciliation.normalize.NormalizedDataSet;
import com.sinchana.reconciliation.normalize.NormalizedRow;
import com.sinchana.reconciliation.model.DataSet;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationEngine {
    private final DataNormalizer normalizer;
    private final RecordMatcher matcher;

    public ReconciliationEngine(DataNormalizer normalizer, RecordMatcher matcher) {
        this.normalizer = normalizer;
        this.matcher = matcher;
    }

    public ReconciliationResult reconcile(DataSet source, DataSet target,
                                          ReconciliationConfiguration configuration) {
        NormalizedDataSet normalizedSource = normalizer.normalize(source, configuration.comparison());
        NormalizedDataSet normalizedTarget = normalizer.normalize(target, configuration.comparison());
        RecordMatcher.MatchResult matches = matcher.match(normalizedSource, configuration.sourceKey(),
                normalizedTarget, configuration.targetKey());
        List<RecordReconciliation> results = new ArrayList<>();
        for (NormalizedRow row : matches.sourceRowsWithoutKey()) {
            results.add(record(ReconciliationStatus.INVALID_KEY, null, row, null, List.of()));
        }
        for (NormalizedRow row : matches.targetRowsWithoutKey()) {
            results.add(record(ReconciliationStatus.INVALID_KEY, null, null, row, List.of()));
        }
        for (Object key : matches.keys()) {
            List<NormalizedRow> sourceRows = matches.sourceByKey().getOrDefault(key, List.of());
            List<NormalizedRow> targetRows = matches.targetByKey().getOrDefault(key, List.of());
            if (sourceRows.size() > 1 || targetRows.size() > 1) {
                results.add(duplicateRecord(key, sourceRows, targetRows));
            } else if (sourceRows.isEmpty()) {
                results.add(record(ReconciliationStatus.EXTRA, key, null, targetRows.get(0), List.of()));
            } else if (targetRows.isEmpty()) {
                results.add(record(ReconciliationStatus.MISSING, key, sourceRows.get(0), null, List.of()));
            } else {
                NormalizedRow sourceRow = sourceRows.get(0);
                NormalizedRow targetRow = targetRows.get(0);
                List<FieldDifference> differences = differences(source, target, sourceRow, targetRow,
                        configuration.sourceKey(), configuration.targetKey());
                results.add(record(differences.isEmpty() ? ReconciliationStatus.MATCHED : ReconciliationStatus.CHANGED,
                        key, sourceRow, targetRow, differences));
            }
        }
        return new ReconciliationResult(results);
    }

    private List<FieldDifference> differences(DataSet source, DataSet target,
                                              NormalizedRow sourceRow, NormalizedRow targetRow,
                                              String sourceKey, String targetKey) {
        Set<String> columns = new LinkedHashSet<>(source.columns());
        columns.addAll(target.columns());
        columns.remove(sourceKey);
        columns.remove(targetKey);
        List<FieldDifference> differences = new ArrayList<>();
        for (String column : columns) {
            Object sourceValue = sourceRow.normalizedValues().get(column);
            Object targetValue = targetRow.normalizedValues().get(column);
            if (!java.util.Objects.equals(sourceValue, targetValue)) {
                differences.add(new FieldDifference(column,
                        sourceRow.originalValues().get(column), targetRow.originalValues().get(column)));
            }
        }
        return differences;
    }

    private RecordReconciliation record(ReconciliationStatus status, Object key,
                                        NormalizedRow source, NormalizedRow target,
                                        List<FieldDifference> differences) {
        return new RecordReconciliation(status, key,
                source == null ? null : source.index(),
                target == null ? null : target.index(),
                source == null ? Map.of() : source.originalValues(),
                target == null ? Map.of() : target.originalValues(),
                source == null ? List.of() : List.of(source.index()),
                target == null ? List.of() : List.of(target.index()),
                source == null ? List.of() : List.of(source.originalValues()),
                target == null ? List.of() : List.of(target.originalValues()),
                differences);
    }

    private RecordReconciliation duplicateRecord(Object key,
                                                 List<NormalizedRow> sourceRows,
                                                 List<NormalizedRow> targetRows) {
        NormalizedRow source = sourceRows.isEmpty() ? null : sourceRows.get(0);
        NormalizedRow target = targetRows.isEmpty() ? null : targetRows.get(0);
        return new RecordReconciliation(
                ReconciliationStatus.DUPLICATE,
                key,
                source == null ? null : source.index(),
                target == null ? null : target.index(),
                source == null ? Map.of() : source.originalValues(),
                target == null ? Map.of() : target.originalValues(),
                sourceRows.stream().map(NormalizedRow::index).toList(),
                targetRows.stream().map(NormalizedRow::index).toList(),
                sourceRows.stream().map(NormalizedRow::originalValues).toList(),
                targetRows.stream().map(NormalizedRow::originalValues).toList(),
                List.of());
    }
}
