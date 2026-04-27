package com.thaca.common.excel.result;

/**
 * Represents a single validation error on a specific cell.
 */
public class RowError {

    private final int rowIndex;
    private final String columnKey;
    private final String columnHeader;
    private final String errorMessage;
    private final Object value;

    public RowError(int rowIndex, String columnKey, String columnHeader, String errorMessage, Object value) {
        this.rowIndex = rowIndex;
        this.columnKey = columnKey;
        this.columnHeader = columnHeader;
        this.errorMessage = errorMessage;
        this.value = value;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public String getColumnHeader() {
        return columnHeader;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format(
            "Row %d [%s] (%s): %s (value=%s)",
            rowIndex + 1,
            columnKey,
            columnHeader,
            errorMessage,
            value
        );
    }
}
