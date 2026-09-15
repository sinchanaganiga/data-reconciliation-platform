package com.sinchana.reconciliation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A reader-independent tabular data model. Linked maps retain the workbook's
 * column order while allowing cell values to keep their native Java types.
 */
public final class DataSet {
    private final String name;
    private final List<String> columns;
    private final List<Map<String, Object>> rows;
    private final Map<String, Object> metadata;

    public DataSet(String name, List<String> columns, List<Map<String, Object>> rows) {
        this(name, columns, rows, Map.of());
    }

    public DataSet(String name, List<String> columns, List<Map<String, Object>> rows,
                   Map<String, Object> metadata) {
        this.name = name;
        this.columns = List.copyOf(columns == null ? List.of() : columns);
        List<Map<String, Object>> copiedRows = new ArrayList<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                LinkedHashMap<String, Object> ordered = new LinkedHashMap<>();
                if (row != null) {
                    for (String column : this.columns) {
                        if (row.containsKey(column)) ordered.put(column, row.get(column));
                    }
                }
                if (row != null) row.forEach((key, value) -> ordered.putIfAbsent(key, value));
                copiedRows.add(Collections.unmodifiableMap(ordered));
            }
        }
        this.rows = List.copyOf(copiedRows);
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(
                metadata == null ? Map.of() : metadata));
    }

    public String name() { return name; }
    public List<String> columns() { return columns; }
    public List<Map<String, Object>> rows() { return rows; }
    public Map<String, Object> metadata() { return metadata; }
    public int rowCount() { return rows.size(); }
    public int columnCount() { return columns.size(); }

    public String getName() { return name; }
    public List<String> getColumns() { return columns; }
    public List<Map<String, Object>> getRows() { return rows; }
    public Map<String, Object> getMetadata() { return metadata; }
}
