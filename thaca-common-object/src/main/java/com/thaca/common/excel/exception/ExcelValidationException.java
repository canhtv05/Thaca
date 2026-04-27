package com.thaca.common.excel.exception;

import com.thaca.common.excel.result.ImportResult;

/**
 * Thrown when Excel data validation fails and failFast is enabled,
 * or when the caller explicitly wants to abort on validation errors.
 * Carries the partial ImportResult for error inspection.
 */
public class ExcelValidationException extends RuntimeException {

    private final ImportResult<?> importResult;

    public ExcelValidationException(String message, ImportResult<?> importResult) {
        super(message);
        this.importResult = importResult;
    }

    public ExcelValidationException(String message) {
        super(message);
        this.importResult = null;
    }

    public ImportResult<?> getImportResult() {
        return importResult;
    }
}
