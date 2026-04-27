package com.thaca.common.excel.schema;

import java.util.Map;

/**
 * Context object passed to custom validators, providing access to the current row's data.
 */
public class RowContext {

    private final int rowIndex;
    private final Map<String, Object> rowData;

    public RowContext(int rowIndex, Map<String, Object> rowData) {
        this.rowIndex = rowIndex;
        this.rowData = rowData;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Returns all parsed values for the current row, keyed by column key.
     */
    public Map<String, Object> getRowData() {
        return rowData;
    }

    /**
     * Gets a specific column value from the current row.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) rowData.get(key);
    }
}
