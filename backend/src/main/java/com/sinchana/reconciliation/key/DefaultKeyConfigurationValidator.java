package com.sinchana.reconciliation.key;

import com.sinchana.reconciliation.exception.DataValidationException;
import com.sinchana.reconciliation.model.DataSet;
import org.springframework.stereotype.Component;

@Component
public class DefaultKeyConfigurationValidator implements KeyConfigurationValidator {
    @Override
    public void validate(ReconciliationConfiguration configuration, DataSet source, DataSet target) {
        if (configuration == null) {
            throw new DataValidationException("Reconciliation configuration is required");
        }
        if (configuration.sourceKey() == null || configuration.sourceKey().isBlank()) {
            throw new DataValidationException("A source key column is required");
        }
        if (configuration.targetKey() == null || configuration.targetKey().isBlank()) {
            throw new DataValidationException("A target key column is required");
        }
        if (source == null || !source.columns().contains(configuration.sourceKey())) {
            throw new DataValidationException("Configured source key column was not found: "
                    + configuration.sourceKey());
        }
        if (target == null || !target.columns().contains(configuration.targetKey())) {
            throw new DataValidationException("Configured target key column was not found: "
                    + configuration.targetKey());
        }
    }
}
