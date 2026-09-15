package com.sinchana.reconciliation.profile;

import com.sinchana.reconciliation.model.DataSet;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataProfilerTest {
    private final DataProfiler profiler = new DataProfiler();

    @Test
    void profilesTypedNumericColumnAndMissingValues() {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("Amount", 2.0);
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("Amount", 4.0);
        Map<String, Object> third = new LinkedHashMap<>();
        third.put("Amount", null);
        DataProfile profile = profiler.profile(new DataSet("data.xlsx", List.of("Amount"),
                List.of(first, second, third), Map.of("sheetName", "Data")));

        ColumnProfile column = profile.columns().get(0);
        assertEquals(DataType.INTEGER, column.dataType());
        assertEquals(3, column.totalCount());
        assertEquals(1, column.nullCount());
        assertEquals(2, column.populatedCount());
        assertEquals(3.0, column.mean());
        assertNull(column.minLength());
    }
}
