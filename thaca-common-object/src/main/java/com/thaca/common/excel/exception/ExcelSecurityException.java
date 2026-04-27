package com.thaca.common.excel.exception;

/**
 * Thrown when an uploaded Excel file fails security checks:
 * - Invalid content type
 * - Invalid file name prefix
 * - Invalid file extension
 */
public class ExcelSecurityException extends RuntimeException {

    public ExcelSecurityException(String message) {
        super(message);
    }
}
