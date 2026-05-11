package com.thaca.common.excel.schema;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Defines the complete schema for an Excel import/export operation.
 * This is the central configuration object — everything is driven by this
 * schema.
 */
@Getter
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
        this.columns = List.copyOf(builder.columns);
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

        public Builder addColumnCondition(boolean condition, Integer index, ExcelColumn column) {
            if (condition) {
                return addColumn(index, column);
            }
            return this;
        }

        public Builder addColumn(Integer index, ExcelColumn column) {
            this.columns.add(index, column);
            return this;
        }

        public Builder addColumnCondition(boolean condition, Integer index, ExcelColumn.Builder columnBuilder) {
            if (condition) {
                return addColumn(index, columnBuilder);
            }
            return this;
        }

        public Builder addColumn(ExcelColumn.Builder columnBuilder) {
            this.columns.add(columnBuilder.build());
            return this;
        }

        public Builder addColumn(Integer index, ExcelColumn.Builder columnBuilder) {
            this.columns.add(index, columnBuilder.build());
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
