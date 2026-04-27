package com.thaca.common.excel.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Result of an Excel import operation.
 * Contains successfully parsed rows, error details, and summary counts.
 *
 * @param <T> the type of mapped data objects (use Map for generic maps)
 */
public class ImportResult<T> {

    private final List<T> successRows;
    private final List<RowError> errors;
    private final int totalRows;

    private ImportResult(List<T> successRows, List<RowError> errors, int totalRows) {
        this.successRows = Collections.unmodifiableList(successRows);
        this.errors = Collections.unmodifiableList(errors);
        this.totalRows = totalRows;
    }

    public List<T> getSuccessRows() {
        return successRows;
    }

    public int getSuccessCount() {
        return successRows.size();
    }

    public List<RowError> getErrors() {
        return errors;
    }

    public int getErrorCount() {
        return errors.size();
    }

    public int getTotalRows() {
        return totalRows;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isAllSuccess() {
        return errors.isEmpty() && !successRows.isEmpty();
    }

    /**
     * Returns error rows grouped by row index.
     */
    public Map<Integer, List<RowError>> getErrorsByRow() {
        Map<Integer, List<RowError>> map = new LinkedHashMap<>();
        for (RowError error : errors) {
            map.computeIfAbsent(error.getRowIndex(), k -> new ArrayList<>()).add(error);
        }
        return map;
    }

    /**
     * Returns the set of row indices that had at least one error.
     */
    public Set<Integer> getErrorRowIndices() {
        Set<Integer> set = new LinkedHashSet<>();
        for (RowError error : errors) {
            set.add(error.getRowIndex());
        }
        return set;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {

        private final List<T> successRows = new ArrayList<>();
        private final List<RowError> errors = new ArrayList<>();
        private int totalRows = 0;

        public Builder<T> addSuccess(T row) {
            this.successRows.add(row);
            return this;
        }

        public Builder<T> addError(RowError error) {
            this.errors.add(error);
            return this;
        }

        public Builder<T> addErrors(List<RowError> errors) {
            this.errors.addAll(errors);
            return this;
        }

        public Builder<T> totalRows(int totalRows) {
            this.totalRows = totalRows;
            return this;
        }

        public List<RowError> getErrors() {
            return errors;
        }

        public ImportResult<T> build() {
            return new ImportResult<>(successRows, errors, totalRows);
        }
    }
}
