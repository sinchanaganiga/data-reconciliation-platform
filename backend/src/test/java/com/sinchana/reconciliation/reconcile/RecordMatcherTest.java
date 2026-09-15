package com.sinchana.reconciliation.reconcile;

import com.sinchana.reconciliation.model.DataSet;
import com.sinchana.reconciliation.normalize.ComparisonConfiguration;
import com.sinchana.reconciliation.normalize.DataNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordMatcherTest {
    @Test
    void retainsAllRowsUnderDuplicateKeys() {
        var source = new DataSet("source", List.of("source_id"),
                List.of(Map.of("source_id", "A"), Map.of("source_id", "A")));
        var target = new DataSet("target", List.of("target_id"),
                List.of(Map.of("target_id", "A")));

        var result = new RecordMatcher().match(
                new DataNormalizer().normalize(source, ComparisonConfiguration.defaults()), "source_id",
                new DataNormalizer().normalize(target, ComparisonConfiguration.defaults()), "target_id");

        assertEquals(2, result.sourceByKey().get("A").size());
        assertEquals(1, result.targetByKey().get("A").size());
    }

    @Test
    void doesNotIndexRowsWithNullOrBlankKeys() {
        var source = new DataSet("source", List.of("source_id"),
                List.of(Map.of("source_id", " "), java.util.Collections.singletonMap("source_id", null)));
        var target = new DataSet("target", List.of("target_id"),
                List.of(Map.of("target_id", " "), java.util.Collections.singletonMap("target_id", null)));

        var result = new RecordMatcher().match(
                new DataNormalizer().normalize(source, ComparisonConfiguration.defaults()), "source_id",
                new DataNormalizer().normalize(target, ComparisonConfiguration.defaults()), "target_id");

        assertEquals(2, result.sourceRowsWithoutKey().size());
        assertEquals(2, result.targetRowsWithoutKey().size());
        assertEquals(0, result.keys().size());
    }
}
