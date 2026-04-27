package com.thaca.common.excel.exception;

/**
 * Thrown when the Excel file structure is invalid:
 * - Missing or unreadable sheet
 * - Corrupted workbook
 * - Header mismatch (strict mode)
 * - Exceeded max rows
 */
public class ExcelFormatException extends RuntimeException {

    public ExcelFormatException(String message) {
        super(message);
    }

    public ExcelFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
