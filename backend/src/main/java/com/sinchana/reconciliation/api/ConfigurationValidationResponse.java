package com.sinchana.reconciliation.api;

import com.sinchana.reconciliation.key.ReconciliationConfiguration;

public record ConfigurationValidationResponse(
        boolean valid,
        ReconciliationConfiguration configuration,
        String message) {
}
