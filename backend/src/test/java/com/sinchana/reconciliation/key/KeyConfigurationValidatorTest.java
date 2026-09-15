package com.sinchana.reconciliation.key;

import com.sinchana.reconciliation.exception.DataValidationException;
import com.sinchana.reconciliation.model.DataSet;
import com.sinchana.reconciliation.normalize.ComparisonConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyConfigurationValidatorTest {
    private final KeyConfigurationValidator validator = new DefaultKeyConfigurationValidator();
    private final DataSet source = new DataSet("source", List.of("left_key", "value"), List.of());
    private final DataSet target = new DataSet("target", List.of("right_key", "value"), List.of());

    @Test
    void acceptsDifferentExistingKeyColumns() {
        var configuration = new ReconciliationConfiguration("left_key", "right_key", ComparisonConfiguration.defaults());
        assertDoesNotThrow(() -> validator.validate(configuration, source, target));
    }

    @Test
    void rejectsMissingKeysAndUnknownColumns() {
        assertEquals("A source key column is required", assertThrows(DataValidationException.class,
                () -> validator.validate(new ReconciliationConfiguration("", "right_key", null), source, target)).getMessage());
        assertEquals("A target key column is required", assertThrows(DataValidationException.class,
                () -> validator.validate(new ReconciliationConfiguration("left_key", null, null), source, target)).getMessage());
        assertThrows(DataValidationException.class,
                () -> validator.validate(new ReconciliationConfiguration("unknown", "right_key", null), source, target));
        assertThrows(DataValidationException.class,
                () -> validator.validate(new ReconciliationConfiguration("left_key", "unknown", null), source, target));
    }

    @Test
    void suppliesSensibleComparisonDefaults() {
        var defaults = ComparisonConfiguration.defaults();
        assertEquals(true, defaults.trimWhitespace());
        assertEquals(false, defaults.normalizeInternalWhitespace());
        assertEquals(true, defaults.caseSensitive());
        assertEquals(true, defaults.normalizeNumbers());
        assertEquals(true, defaults.normalizeDates());
        assertEquals(false, defaults.nullEqualsBlank());
        assertEquals(defaults, new ReconciliationConfiguration("left_key", "right_key", null).comparison());
    }
}
