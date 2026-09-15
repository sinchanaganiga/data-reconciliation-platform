package com.sinchana.reconciliation.normalize;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record NormalizedRow(
        int index,
        Map<String, Object> originalValues,
        Map<String, Object> normalizedValues) {

    public NormalizedRow {
        originalValues = Collections.unmodifiableMap(new LinkedHashMap<>(originalValues));
        normalizedValues = Collections.unmodifiableMap(new LinkedHashMap<>(normalizedValues));
    }
}
