package com.sinchana.reconciliation.reconcile;

import com.sinchana.reconciliation.normalize.NormalizedDataSet;
import com.sinchana.reconciliation.normalize.NormalizedRow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RecordMatcher {
    public MatchResult match(NormalizedDataSet source, String sourceKey,
                             NormalizedDataSet target, String targetKey) {
        IndexResult sourceIndex = index(source.rows(), sourceKey);
        IndexResult targetIndex = index(target.rows(), targetKey);
        Map<Object, List<NormalizedRow>> sourceByKey = sourceIndex.rowsByKey();
        Map<Object, List<NormalizedRow>> targetByKey = targetIndex.rowsByKey();
        Set<Object> keys = new LinkedHashSet<>(sourceByKey.keySet());
        keys.addAll(targetByKey.keySet());
        return new MatchResult(sourceByKey, targetByKey, keys,
                sourceIndex.rowsWithoutKey(), targetIndex.rowsWithoutKey());
    }

    private IndexResult index(List<NormalizedRow> rows, String keyColumn) {
        Map<Object, List<NormalizedRow>> index = new LinkedHashMap<>();
        List<NormalizedRow> rowsWithoutKey = new ArrayList<>();
        for (NormalizedRow row : rows) {
            Object key = row.normalizedValues().get(keyColumn);
            if (key == null || key instanceof String value && value.isBlank()) {
                rowsWithoutKey.add(row);
            } else {
                index.computeIfAbsent(key, ignored -> new ArrayList<>()).add(row);
            }
        }
        return new IndexResult(index, rowsWithoutKey);
    }

    private record IndexResult(
            Map<Object, List<NormalizedRow>> rowsByKey,
            List<NormalizedRow> rowsWithoutKey) {
    }

    public record MatchResult(
            Map<Object, List<NormalizedRow>> sourceByKey,
            Map<Object, List<NormalizedRow>> targetByKey,
            Collection<Object> keys,
            List<NormalizedRow> sourceRowsWithoutKey,
            List<NormalizedRow> targetRowsWithoutKey) {
    }
}
