package com.thaca.common.excel.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines the complete schema for an Excel import/export operation.
 * This is the central configuration object — everything is driven by this schema.
 */
public class ExcelSchema {

    private final String sheetName;
    private final int headerRowIndex;
    private final int dataStartRowIndex;
    private final boolean strictHeader;
    private final boolean failFast;
    private final int maxRows;
    private final List<ExcelColumn> columns;

    private ExcelSchema(Builder builder) {
        this.sheetName = builder.sheetName;
        this.headerRowIndex = builder.headerRowIndex;
        this.dataStartRowIndex = builder.dataStartRowIndex;
        this.strictHeader = builder.strictHeader;
        this.failFast = builder.failFast;
        this.maxRows = builder.maxRows;
        this.columns = Collections.unmodifiableList(new ArrayList<>(builder.columns));
    }

    public String getSheetName() {
        return sheetName;
    }

    public int getHeaderRowIndex() {
        return headerRowIndex;
    }

    public int getDataStartRowIndex() {
        return dataStartRowIndex;
    }

    public boolean isStrictHeader() {
        return strictHeader;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public List<ExcelColumn> getColumns() {
        return columns;
    }

    /**
     * Finds a column by its key. Returns null if not found.
     */
    public ExcelColumn getColumn(String key) {
        for (ExcelColumn col : columns) {
            if (col.getKey().equals(key)) {
                return col;
            }
        }
        return null;
    }

    /**
     * Returns the total number of defined columns.
     */
    public int getColumnCount() {
        return columns.size();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String sheetName = "Sheet1";
        private int headerRowIndex = 0;
        private int dataStartRowIndex = 1;
        private boolean strictHeader = true;
        private boolean failFast = false;
        private int maxRows = 50000;
        private final List<ExcelColumn> columns = new ArrayList<>();

        public Builder sheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        public Builder headerRowIndex(int headerRowIndex) {
            this.headerRowIndex = headerRowIndex;
            return this;
        }

        public Builder dataStartRowIndex(int dataStartRowIndex) {
            this.dataStartRowIndex = dataStartRowIndex;
            return this;
        }

        public Builder strictHeader(boolean strictHeader) {
            this.strictHeader = strictHeader;
            return this;
        }

        public Builder failFast(boolean failFast) {
            this.failFast = failFast;
            return this;
        }

        public Builder maxRows(int maxRows) {
            this.maxRows = maxRows;
            return this;
        }

        public Builder addColumn(ExcelColumn column) {
            this.columns.add(column);
            return this;
        }

        public Builder addColumn(ExcelColumn.Builder columnBuilder) {
            this.columns.add(columnBuilder.build());
            return this;
        }

        public ExcelSchema build() {
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("ExcelSchema must have at least one column");
            }
            if (dataStartRowIndex <= headerRowIndex) {
                throw new IllegalArgumentException("dataStartRowIndex must be greater than headerRowIndex");
            }
            return new ExcelSchema(this);
        }
    }
}
