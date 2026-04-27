package com.thaca.common.excel.writer;

import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.common.excel.style.ExcelStyleFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Exports data to Excel using streaming (SXSSFWorkbook) for memory efficiency.
 * Supports 50k+ rows without memory issues.
 * <p>
 * Data is provided as List of Maps (key → value), mapped by ExcelColumn keys.
 */
public final class ExcelExporter {

    private static final int STREAMING_WINDOW_SIZE = 500;

    private ExcelExporter() {}

    /**
     * Exports data to the given output stream using streaming workbook.
     *
     * @param schema the schema defining columns and sheet config
     * @param data   list of row maps, keyed by column key
     * @param out    the output stream to write to
     */
    public static void export(ExcelSchema schema, List<Map<String, Object>> data, OutputStream out) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(STREAMING_WINDOW_SIZE)) {
            workbook.setCompressTempFiles(true);
            SXSSFSheet sheet = workbook.createSheet(schema.getSheetName());
            writeSheet(workbook, sheet, schema, data);
            workbook.write(out);
        }
    }

    /**
     * Exports data and returns as byte array.
     */
    public static byte[] exportAsBytes(ExcelSchema schema, List<Map<String, Object>> data) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            export(schema, data, bos);
            return bos.toByteArray();
        }
    }

    /**
     * Exports a list of typed objects using a mapper function.
     *
     * @param schema    the schema defining columns
     * @param items     the typed data list
     * @param rowMapper function to convert each item to a Map of column key → value
     * @param out       the output stream
     */
    public static <T> void exportObjects(
        ExcelSchema schema,
        List<T> items,
        Function<T, Map<String, Object>> rowMapper,
        OutputStream out
    ) throws IOException {
        List<Map<String, Object>> data = items.stream().map(rowMapper).toList();
        export(schema, data, out);
    }

    /**
     * Exports typed objects and returns as byte array.
     */
    public static <T> byte[] exportObjectsAsBytes(
        ExcelSchema schema,
        List<T> items,
        Function<T, Map<String, Object>> rowMapper
    ) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            exportObjects(schema, items, rowMapper, bos);
            return bos.toByteArray();
        }
    }

    // ─── Internal ────────────────────────────────────────────

    private static void writeSheet(
        SXSSFWorkbook workbook,
        SXSSFSheet sheet,
        ExcelSchema schema,
        List<Map<String, Object>> data
    ) {
        // Styles
        CellStyle headerStyle = ExcelStyleFactory.createHeaderStyle(workbook);
        CellStyle requiredHeaderStyle = ExcelStyleFactory.createRequiredHeaderStyle(workbook);
        CellStyle dataStyle = ExcelStyleFactory.createDataStyle(workbook);
        CellStyle numberStyle = ExcelStyleFactory.createNumberStyle(workbook);

        // Track max column widths for auto-sizing
        int[] maxWidths = new int[schema.getColumnCount()];

        // Write header row
        Row headerRow = sheet.createRow(schema.getHeaderRowIndex());
        headerRow.setHeightInPoints(28);

        List<ExcelColumn> columns = schema.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            ExcelColumn col = columns.get(i);
            Cell cell = headerRow.createCell(i);
            String headerText = col.getDisplayHeader();
            cell.setCellValue(headerText);
            cell.setCellStyle(col.isRequired() ? requiredHeaderStyle : headerStyle);
            maxWidths[i] = headerText.length();
        }

        // Write data rows
        for (int rowIdx = 0; rowIdx < data.size(); rowIdx++) {
            Map<String, Object> rowData = data.get(rowIdx);
            Row row = sheet.createRow(schema.getDataStartRowIndex() + rowIdx);

            for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
                ExcelColumn col = columns.get(colIdx);
                Cell cell = row.createCell(colIdx);
                Object value = rowData.get(col.getKey());

                if (value == null) {
                    cell.setCellStyle(dataStyle);
                    continue;
                }

                writeTypedValue(cell, value, col, dataStyle, numberStyle, workbook);

                // Track max width
                int len = value.toString().length();
                if (len > maxWidths[colIdx]) {
                    maxWidths[colIdx] = Math.min(len, 60); // Cap at 60 chars
                }
            }
        }

        // Apply column widths (SXSSFSheet doesn't support autoSizeColumn well)
        for (int i = 0; i < maxWidths.length; i++) {
            int width = (maxWidths[i] + 3) * 256; // 256 units per character
            width = Math.max(width, 3000); // Minimum width
            width = Math.min(width, 15000); // Maximum width
            sheet.setColumnWidth(i, width);
        }

        // Freeze header
        sheet.createFreezePane(0, schema.getDataStartRowIndex());
    }

    private static void writeTypedValue(
        Cell cell,
        Object value,
        ExcelColumn col,
        CellStyle dataStyle,
        CellStyle numberStyle,
        SXSSFWorkbook workbook
    ) {
        switch (col.getDataType()) {
            case NUMBER:
                if (value instanceof Number num) {
                    cell.setCellValue(num.doubleValue());
                } else {
                    try {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    } catch (NumberFormatException e) {
                        cell.setCellValue(value.toString());
                    }
                }
                cell.setCellStyle(numberStyle);
                break;
            case BOOLEAN:
                if (value instanceof Boolean bool) {
                    cell.setCellValue(bool);
                } else {
                    cell.setCellValue(Boolean.parseBoolean(value.toString()));
                }
                cell.setCellStyle(dataStyle);
                break;
            case DATE:
                CellStyle dateStyle = ExcelStyleFactory.createDateStyle(
                    workbook,
                    col.getDateFormat() != null ? col.getDateFormat() : "yyyy-MM-dd"
                );
                if (value instanceof Date date) {
                    cell.setCellValue(date);
                } else if (value instanceof java.time.LocalDate ld) {
                    cell.setCellValue(ld);
                } else if (value instanceof java.time.LocalDateTime ldt) {
                    cell.setCellValue(ldt);
                } else {
                    cell.setCellValue(value.toString());
                }
                cell.setCellStyle(dateStyle);
                break;
            case STRING:
            default:
                cell.setCellValue(value.toString());
                cell.setCellStyle(dataStyle);
                break;
        }
    }
}
