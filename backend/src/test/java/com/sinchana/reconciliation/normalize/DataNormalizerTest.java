package com.sinchana.reconciliation.normalize;

import com.sinchana.reconciliation.model.DataSet;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataNormalizerTest {
    @Test
    void normalizesConfiguredValuesWithoutChangingOriginalRows() {
        var row = new LinkedHashMap<String, Object>();
        row.put("id", " 001.00 ");
        row.put("name", "  Ada   Lovelace  ");
        var dataSet = new DataSet("source", List.of("id", "name"), List.of(row));
        var configuration = new ComparisonConfiguration(true, true, false, true, false, false);

        var normalized = new DataNormalizer().normalize(dataSet, configuration);

        assertEquals(" 001.00 ", dataSet.rows().get(0).get("id"));
        assertEquals("1", normalized.rows().get(0).normalizedValues().get("id"));
        assertEquals("ada lovelace", normalized.rows().get(0).normalizedValues().get("name"));
        assertEquals(row, normalized.rows().get(0).originalValues());
    }

    @Test
    void normalizesDateOnlyAndMidnightDateTimeToTheSameInstant() {
        var row = new LinkedHashMap<String, Object>();
        row.put("dateOnly", LocalDate.of(2024, 1, 1));
        row.put("dateTime", LocalDateTime.of(2024, 1, 1, 0, 0));
        row.put("dateText", "2024-01-01");
        row.put("dateTimeText", "2024-01-01T00:00:00");
        var dataSet = new DataSet("source",
                List.of("dateOnly", "dateTime", "dateText", "dateTimeText"), List.of(row));

        var normalized = new DataNormalizer().normalize(dataSet, ComparisonConfiguration.defaults());

        assertEquals("2024-01-01T00:00:00Z", normalized.rows().get(0).normalizedValues().get("dateOnly"));
        assertEquals(normalized.rows().get(0).normalizedValues().get("dateOnly"),
                normalized.rows().get(0).normalizedValues().get("dateTime"));
        assertEquals(normalized.rows().get(0).normalizedValues().get("dateOnly"),
                normalized.rows().get(0).normalizedValues().get("dateText"));
        assertEquals(normalized.rows().get(0).normalizedValues().get("dateOnly"),
                normalized.rows().get(0).normalizedValues().get("dateTimeText"));
    }
}
