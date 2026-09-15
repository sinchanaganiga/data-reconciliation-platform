package com.sinchana.reconciliation.api;

import com.sinchana.reconciliation.key.DefaultKeyConfigurationValidator;
import com.sinchana.reconciliation.key.ReconciliationConfiguration;
import com.sinchana.reconciliation.model.DataSet;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reconciliation")
public class ConfigurationController {
    private final DefaultKeyConfigurationValidator validator;

    public ConfigurationController(DefaultKeyConfigurationValidator validator) {
        this.validator = validator;
    }

    @PostMapping("/configuration/validate")
    public ConfigurationValidationResponse validate(@RequestBody ConfigurationValidationRequest request) {
        if (request == null) {
            throw new com.sinchana.reconciliation.exception.DataValidationException("Configuration request is required");
        }
        ReconciliationConfiguration configuration = request.configuration();
        DataSet source = new DataSet("source", request.sourceColumns() == null ? List.of() : request.sourceColumns(),
                List.of());
        DataSet target = new DataSet("target", request.targetColumns() == null ? List.of() : request.targetColumns(),
                List.of());
        validator.validate(configuration, source, target);
        return new ConfigurationValidationResponse(true, configuration, "Configuration is valid");
    }
}
