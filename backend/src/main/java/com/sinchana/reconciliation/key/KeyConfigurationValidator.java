package com.sinchana.reconciliation.key;

import com.sinchana.reconciliation.model.DataSet;
import org.springframework.stereotype.Component;

public interface KeyConfigurationValidator {
    void validate(ReconciliationConfiguration configuration, DataSet source, DataSet target);
}
