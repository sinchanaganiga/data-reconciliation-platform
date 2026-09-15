package com.sinchana.reconciliation.key;

import com.sinchana.reconciliation.normalize.ComparisonConfiguration;

import java.util.Objects;

public record ReconciliationConfiguration(
        String sourceKey,
        String targetKey,
        ComparisonConfiguration comparison) {

    public ReconciliationConfiguration {
        comparison = Objects.requireNonNullElseGet(comparison, ComparisonConfiguration::defaults);
    }
}
