package com.thaca.common.excel.reader;

import com.thaca.common.excel.exception.ExcelFormatException;
import com.thaca.common.excel.exception.ExcelSecurityException;
import com.thaca.common.excel.exception.ExcelValidationException;
import com.thaca.common.excel.result.ImportResult;
import com.thaca.common.excel.result.RowError;
import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.common.excel.schema.RowContext;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

/**
 * Reads and validates Excel files according to an {@link ExcelSchema}.
 * <p>
 * Supports:
 * - Security checks on MultipartFile (content type, name prefix, extension)
 * - Header validation (strict mode)
 * - Cell-level validation (required, data type, length, range, allowed values,
 * custom)
 * - Fail-fast or collect-all-errors mode
 * - Max row limit enforcement
 */
public final class ExcelReader {

    private static final String ALLOWED_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String REQUIRED_PREFIX = "thaca-";
    private static final String REQUIRED_EXTENSION = ".xlsx";

    private ExcelReader() {}

    // ─── Public API ──────────────────────────────────────────

    /**
     * Reads and validates a MultipartFile with full security checks.
     */
    public static ImportResult<Map<String, Object>> read(MultipartFile file, ExcelSchema schema) {
        validateFile(file);
        try (InputStream is = file.getInputStream()) {
            return readFromStream(is, schema);
        } catch (IOException e) {
            throw new ExcelFormatException("Failed to read uploaded file: " + e.getMessage(), e);
        }
    }

    /**
     * Reads and validates raw bytes with full security checks.
     */
    public static ImportResult<Map<String, Object>> read(
        byte[] bytes,
        String fileName,
        String contentType,
        ExcelSchema schema
    ) {
        validateFile(fileName, contentType);
        if (bytes == null || bytes.length == 0) {
            throw new ExcelSecurityException("File content must not be empty");
        }
        try (InputStream is = new java.io.ByteArrayInputStream(bytes)) {
            return readFromStream(is, schema);
        } catch (IOException e) {
            throw new ExcelFormatException("Failed to process file bytes: " + e.getMessage(), e);
        }
    }

    /**
     * Reads and validates from a raw InputStream (no security checks).
     * The caller is responsible for closing the stream.
     */
    public static ImportResult<Map<String, Object>> readFromStream(InputStream inputStream, ExcelSchema schema) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            return processWorkbook(workbook, schema);
        } catch (ExcelFormatException | ExcelValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelFormatException("Failed to parse Excel file: " + e.getMessage(), e);
        }
    }

    // ─── Security Validation ─────────────────────────────────

    /**
     * Validates file security: content type, name prefix, and extension.
     */
    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ExcelSecurityException("File must not be empty");
        }
        validateFile(file.getOriginalFilename(), file.getContentType());
    }

    /**
     * Validates file security via raw fileName and contentType.
     */
    public static void validateFile(String originalFilename, String contentType) {
        if (contentType == null || !contentType.equals(ALLOWED_CONTENT_TYPE)) {
            throw new ExcelSecurityException(
                "Invalid content type: " + contentType + ". Expected: " + ALLOWED_CONTENT_TYPE
            );
        }

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ExcelSecurityException("File name must not be empty");
        }

        String lowerName = originalFilename.toLowerCase();
        if (!lowerName.startsWith(REQUIRED_PREFIX)) {
            throw new ExcelSecurityException(
                "File name must start with '" + REQUIRED_PREFIX + "'. Got: " + originalFilename
            );
        }
        if (!lowerName.endsWith(REQUIRED_EXTENSION)) {
            throw new ExcelSecurityException(
                "File must have '" + REQUIRED_EXTENSION + "' extension. Got: " + originalFilename
            );
        }
    }

    // ─── Core Processing ─────────────────────────────────────

    private static ImportResult<Map<String, Object>> processWorkbook(Workbook workbook, ExcelSchema schema) {
        Sheet sheet = workbook.getSheet(schema.getSheetName());
        if (sheet == null) {
            sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new ExcelFormatException("No sheet found in workbook");
            }
        }

        // Validate header if strict mode
        Map<Integer, ExcelColumn> columnMapping;
        if (schema.isStrictHeader()) {
            columnMapping = validateAndMapHeaders(sheet, schema);
        } else {
            columnMapping = buildColumnMapping(sheet, schema);
        }

        // Count data rows
        int lastRow = sheet.getLastRowNum();
        int dataRows = lastRow - schema.getDataStartRowIndex() + 1;
        if (dataRows > schema.getMaxRows()) {
            throw new ExcelFormatException(
                "File contains " + dataRows + " data rows, exceeding the limit of " + schema.getMaxRows()
            );
        }

        // Process data rows
        ImportResult.Builder<Map<String, Object>> resultBuilder = ImportResult.builder();
        int totalProcessed = 0;

        for (int rowIdx = schema.getDataStartRowIndex(); rowIdx <= lastRow; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null || isEmptyRow(row)) {
                continue;
            }

            totalProcessed++;
            Map<String, Object> rowData = new LinkedHashMap<>();
            List<RowError> rowErrors = new ArrayList<>();

            // First pass: parse all cell values
            for (Map.Entry<Integer, ExcelColumn> entry : columnMapping.entrySet()) {
                int colIdx = entry.getKey();
                ExcelColumn column = entry.getValue();
                Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String rawValue = getCellValueAsString(cell);
                Object parsedValue = parseValue(rawValue, column, rowIdx, rowErrors);
                rowData.put(column.getKey(), parsedValue);
            }

            // Second pass: validate all values (with full row context)
            RowContext context = new RowContext(rowIdx, rowData);
            for (Map.Entry<Integer, ExcelColumn> entry : columnMapping.entrySet()) {
                ExcelColumn column = entry.getValue();
                Object value = rowData.get(column.getKey());
                validateCell(value, column, context, rowErrors);
            }

            if (rowErrors.isEmpty()) {
                resultBuilder.addSuccess(rowData);
            } else {
                resultBuilder.addErrors(rowErrors);
                if (schema.isFailFast()) {
                    resultBuilder.totalRows(totalProcessed);
                    ImportResult<Map<String, Object>> partial = resultBuilder.build();
                    throw new ExcelValidationException("Validation failed at row " + (rowIdx + 1), partial);
                }
            }
        }

        resultBuilder.totalRows(totalProcessed);
        return resultBuilder.build();
    }

    // ─── Header Validation ───────────────────────────────────

    private static Map<Integer, ExcelColumn> validateAndMapHeaders(Sheet sheet, ExcelSchema schema) {
        Row headerRow = sheet.getRow(schema.getHeaderRowIndex());
        if (headerRow == null) {
            throw new ExcelFormatException("Header row not found at index " + schema.getHeaderRowIndex());
        }

        // Build actual header map from file
        Map<String, Integer> actualHeaders = new LinkedHashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.isBlank()) {
                    actualHeaders.put(normalizeHeader(value), i);
                }
            }
        }

        // Map schema columns to file columns
        Map<Integer, ExcelColumn> mapping = new LinkedHashMap<>();
        List<String> missingHeaders = new ArrayList<>();

        for (ExcelColumn column : schema.getColumns()) {
            String normalized = normalizeHeader(column.getHeader());
            String normalizedDisplay = normalizeHeader(column.getDisplayHeader());

            Integer colIdx = actualHeaders.get(normalized);
            if (colIdx == null) {
                colIdx = actualHeaders.get(normalizedDisplay);
            }

            if (colIdx != null) {
                mapping.put(colIdx, column);
            } else if (column.isRequired()) {
                missingHeaders.add(column.getHeader());
            }
        }

        if (!missingHeaders.isEmpty()) {
            throw new ExcelFormatException("Missing required headers: " + String.join(", ", missingHeaders));
        }

        return mapping;
    }

    private static Map<Integer, ExcelColumn> buildColumnMapping(Sheet sheet, ExcelSchema schema) {
        Row headerRow = sheet.getRow(schema.getHeaderRowIndex());
        Map<Integer, ExcelColumn> mapping = new LinkedHashMap<>();

        if (headerRow != null) {
            Map<String, Integer> actualHeaders = new LinkedHashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null && !value.isBlank()) {
                        actualHeaders.put(normalizeHeader(value), i);
                    }
                }
            }

            for (ExcelColumn column : schema.getColumns()) {
                String normalized = normalizeHeader(column.getHeader());
                String normalizedDisplay = normalizeHeader(column.getDisplayHeader());
                Integer colIdx = actualHeaders.get(normalized);
                if (colIdx == null) {
                    colIdx = actualHeaders.get(normalizedDisplay);
                }
                if (colIdx != null) {
                    mapping.put(colIdx, column);
                }
            }
        }

        // Fallback: map by position if header matching yielded nothing
        if (mapping.isEmpty()) {
            for (int i = 0; i < schema.getColumns().size(); i++) {
                mapping.put(i, schema.getColumns().get(i));
            }
        }

        return mapping;
    }

    // ─── Cell Parsing ────────────────────────────────────────

    private static Object parseValue(String rawValue, ExcelColumn column, int rowIdx, List<RowError> errors) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        // Apply custom value mapper first
        if (column.getValueMapper() != null) {
            try {
                return column.getValueMapper().apply(rawValue);
            } catch (Exception e) {
                errors.add(
                    new RowError(
                        rowIdx,
                        column.getKey(),
                        column.getHeader(),
                        "Value mapping failed: " + e.getMessage(),
                        rawValue
                    )
                );
                return rawValue;
            }
        }

        // Parse based on data type
        switch (column.getDataType()) {
            case NUMBER:
                try {
                    String cleaned = rawValue.replace(",", "").trim();
                    if (cleaned.contains(".")) {
                        return Double.parseDouble(cleaned);
                    }
                    return Long.parseLong(cleaned);
                } catch (NumberFormatException e) {
                    errors.add(
                        new RowError(rowIdx, column.getKey(), column.getHeader(), "Invalid number format", rawValue)
                    );
                    return rawValue;
                }
            case BOOLEAN:
                String lower = rawValue.toLowerCase().trim();
                if ("true".equals(lower) || "1".equals(lower) || "yes".equals(lower) || "có".equals(lower)) {
                    return Boolean.TRUE;
                } else if ("false".equals(lower) || "0".equals(lower) || "no".equals(lower) || "không".equals(lower)) {
                    return Boolean.FALSE;
                } else {
                    errors.add(
                        new RowError(
                            rowIdx,
                            column.getKey(),
                            column.getHeader(),
                            "Invalid boolean value (expected: true/false/yes/no/1/0)",
                            rawValue
                        )
                    );
                    return rawValue;
                }
            case DATE:
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(column.getDateFormat());
                    sdf.setLenient(false);
                    return sdf.parse(rawValue);
                } catch (Exception e) {
                    errors.add(
                        new RowError(
                            rowIdx,
                            column.getKey(),
                            column.getHeader(),
                            "Invalid date format, expected: " + column.getDateFormat(),
                            rawValue
                        )
                    );
                    return rawValue;
                }
            case STRING:
            default:
                return rawValue.trim();
        }
    }

    // ─── Cell Validation ─────────────────────────────────────

    private static void validateCell(Object value, ExcelColumn column, RowContext context, List<RowError> errors) {
        int rowIdx = context.getRowIndex();

        // Required check
        if (column.isRequired() && (value == null || value.toString().isBlank())) {
            errors.add(new RowError(rowIdx, column.getKey(), column.getHeader(), "Value is required", value));
            return; // Skip further validation for empty required fields
        }

        if (value == null || value.toString().isBlank()) {
            return; // Optional empty field — skip further checks
        }

        String stringValue = value.toString();

        // Max length check
        if (column.getMaxLength() != null && stringValue.length() > column.getMaxLength()) {
            errors.add(
                new RowError(
                    rowIdx,
                    column.getKey(),
                    column.getHeader(),
                    "Exceeds max length of " + column.getMaxLength() + " (actual: " + stringValue.length() + ")",
                    value
                )
            );
        }

        // Range check (for numbers)
        if (value instanceof Number number) {
            double numVal = number.doubleValue();
            if (column.getMinValue() != null && numVal < column.getMinValue()) {
                errors.add(
                    new RowError(
                        rowIdx,
                        column.getKey(),
                        column.getHeader(),
                        "Value " + numVal + " is less than minimum " + column.getMinValue(),
                        value
                    )
                );
            }
            if (column.getMaxValue() != null && numVal > column.getMaxValue()) {
                errors.add(
                    new RowError(
                        rowIdx,
                        column.getKey(),
                        column.getHeader(),
                        "Value " + numVal + " exceeds maximum " + column.getMaxValue(),
                        value
                    )
                );
            }
        }

        // Allowed values check
        if (column.getAllowedValues() != null && !column.getAllowedValues().isEmpty()) {
            boolean found = false;
            for (String allowed : column.getAllowedValues()) {
                if (allowed.equalsIgnoreCase(stringValue)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                errors.add(
                    new RowError(
                        rowIdx,
                        column.getKey(),
                        column.getHeader(),
                        "Value '" + stringValue + "' is not in allowed values: " + column.getAllowedValues(),
                        value
                    )
                );
            }
        }

        // Custom validator
        if (column.getCustomValidator() != null) {
            String errorMsg = column.getCustomValidator().apply(value, context);
            if (errorMsg != null && !errorMsg.isBlank()) {
                errors.add(new RowError(rowIdx, column.getKey(), column.getHeader(), errorMsg, value));
            }
        }
    }

    // ─── Utilities ───────────────────────────────────────────

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    yield sdf.format(cell.getDateCellValue());
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    yield String.valueOf((long) d);
                }
                yield String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        yield String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        yield null;
                    }
                }
            }
            case BLANK, ERROR -> null;
            default -> null;
        };
    }

    private static boolean isEmptyRow(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellValueAsString(cell);
                if (val != null && !val.isBlank()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Normalizes header text for matching: lowercase, trim, remove (*) marks.
     */
    private static String normalizeHeader(String header) {
        if (header == null) return "";
        return header.toLowerCase().replace("*", "").replace("*", "").replaceAll("\\s+", " ").trim();
    }
}
