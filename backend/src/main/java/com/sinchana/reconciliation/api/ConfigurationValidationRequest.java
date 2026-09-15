package com.sinchana.reconciliation.api;

import com.sinchana.reconciliation.key.ReconciliationConfiguration;

import java.util.List;

public record ConfigurationValidationRequest(
        List<String> sourceColumns,
        List<String> targetColumns,
        ReconciliationConfiguration configuration) {
}
