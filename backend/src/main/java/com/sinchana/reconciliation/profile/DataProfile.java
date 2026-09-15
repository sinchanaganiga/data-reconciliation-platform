package com.sinchana.reconciliation.profile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DataProfile {
    private final int rowCount;
    private final int columnCount;
    private final List<ColumnProfile> columns;
    private final Map<String, Object> metadata;

    public DataProfile(int rowCount, int columnCount, List<ColumnProfile> columns) {
        this(rowCount, columnCount, columns, Map.of());
    }

    public DataProfile(int rowCount, int columnCount, List<ColumnProfile> columns,
                       Map<String, Object> metadata) {
        this.rowCount = rowCount; this.columnCount = columnCount;
        this.columns = List.copyOf(columns);
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(metadata == null ? Map.of() : metadata));
    }

    public int rowCount() { return rowCount; }
    public int columnCount() { return columnCount; }
    public int totalRows() { return rowCount; }
    public int totalColumns() { return columnCount; }
    public List<ColumnProfile> columns() { return columns; }
    public Map<String, Object> metadata() { return metadata; }
    public int getRowCount() { return rowCount; }
    public int getColumnCount() { return columnCount; }
    public int getTotalRows() { return rowCount; }
    public int getTotalColumns() { return columnCount; }
    public List<ColumnProfile> getColumns() { return columns; }
    public Map<String, Object> getMetadata() { return metadata; }
}
