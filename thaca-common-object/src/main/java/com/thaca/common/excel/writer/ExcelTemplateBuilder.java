package com.thaca.common.excel.writer;

import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelDataType;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.common.excel.style.ExcelStyleFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Builds download-ready Excel templates from an {@link ExcelSchema}.
 * <p>
 * Features:
 * - Header row with required mark (*)
 * - Data validation dropdowns for columns with allowedValues
 * - Cell comments with instructions
 * - Freeze pane on header
 * - Auto column width
 * - Professional styling
 */
public final class ExcelTemplateBuilder {

    private static final int MAX_DROPDOWN_ROWS = 1000;

    private ExcelTemplateBuilder() {}

    /**
     * Builds a template workbook and writes it to the given output stream.
     */
    public static void build(ExcelSchema schema, OutputStream outputStream) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(schema.getSheetName());
            buildSheet(workbook, sheet, schema);
            workbook.write(outputStream);
        }
    }

    /**
     * Builds a template workbook and returns it as a byte array.
     */
    public static byte[] buildAsBytes(ExcelSchema schema) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            build(schema, bos);
            return bos.toByteArray();
        }
    }

    /**
     * Builds a template and returns the workbook (caller must close).
     */
    public static XSSFWorkbook buildWorkbook(ExcelSchema schema) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(schema.getSheetName());
        buildSheet(workbook, sheet, schema);
        return workbook;
    }

    // ─── Internal ────────────────────────────────────────────

    private static void buildSheet(XSSFWorkbook workbook, XSSFSheet sheet, ExcelSchema schema) {
        CellStyle headerStyle = ExcelStyleFactory.createHeaderStyle(workbook);
        CellStyle requiredHeaderStyle = ExcelStyleFactory.createRequiredHeaderStyle(workbook);
        CreationHelper creationHelper = workbook.getCreationHelper();

        // Create header row
        Row headerRow = sheet.createRow(schema.getHeaderRowIndex());
        headerRow.setHeightInPoints(28);

        for (int i = 0; i < schema.getColumns().size(); i++) {
            ExcelColumn column = schema.getColumns().get(i);
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(column.getDisplayHeader());
            cell.setCellStyle(column.isRequired() ? requiredHeaderStyle : headerStyle);

            // Add comment
            String commentText = buildCommentText(column);
            if (commentText != null) {
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = creationHelper.createClientAnchor();
                anchor.setCol1(i);
                anchor.setCol2(i + 3);
                anchor.setRow1(schema.getHeaderRowIndex());
                anchor.setRow2(schema.getHeaderRowIndex() + 5);
                Comment comment = drawing.createCellComment(anchor);
                comment.setString(creationHelper.createRichTextString(commentText));
                comment.setAuthor("Thaca System");
                cell.setCellComment(comment);
            }

            // Add data validation dropdown
            if (column.getAllowedValues() != null && !column.getAllowedValues().isEmpty()) {
                addDropdownValidation(sheet, i, schema.getDataStartRowIndex(), column);
            }

            // Auto column width
            int headerWidth = column.getDisplayHeader().length() * 350;
            int minWidth = 3500; // ~12 characters
            sheet.setColumnWidth(i, Math.max(headerWidth, minWidth));
        }

        // Freeze header
        sheet.createFreezePane(0, schema.getDataStartRowIndex());

        // Set default column width for better readability
        sheet.setDefaultColumnWidth(15);
    }

    private static String buildCommentText(ExcelColumn column) {
        StringBuilder sb = new StringBuilder();
        sb.append("Column: ").append(column.getHeader()).append("\n");
        sb.append("Type: ").append(column.getDataType().name()).append("\n");

        if (column.isRequired()) {
            sb.append("⚠ REQUIRED\n");
        }

        if (column.getDataType() == ExcelDataType.DATE) {
            sb.append("Format: ").append(column.getDateFormat()).append("\n");
        }

        if (column.getMaxLength() != null) {
            sb.append("Max length: ").append(column.getMaxLength()).append("\n");
        }

        if (column.getMinValue() != null || column.getMaxValue() != null) {
            sb.append("Range: ");
            if (column.getMinValue() != null) sb.append(column.getMinValue());
            sb.append(" ~ ");
            if (column.getMaxValue() != null) sb.append(column.getMaxValue());
            sb.append("\n");
        }

        if (column.getAllowedValues() != null && !column.getAllowedValues().isEmpty()) {
            sb.append("Allowed: ").append(String.join(", ", column.getAllowedValues())).append("\n");
        }

        if (column.getComment() != null) {
            sb.append("\n").append(column.getComment());
        }

        return sb.toString();
    }

    private static void addDropdownValidation(XSSFSheet sheet, int colIdx, int dataStartRow, ExcelColumn column) {
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        String[] values = column.getAllowedValues().toArray(new String[0]);
        DataValidationConstraint constraint = dvHelper.createExplicitListConstraint(values);
        CellRangeAddressList addressList = new CellRangeAddressList(
            dataStartRow,
            dataStartRow + MAX_DROPDOWN_ROWS,
            colIdx,
            colIdx
        );
        DataValidation validation = dvHelper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox(
            "Invalid value",
            "Please select from: " + String.join(", ", column.getAllowedValues())
        );
        validation.setShowPromptBox(true);
        validation.createPromptBox(column.getHeader(), "Select a value from the dropdown");
        sheet.addValidationData(validation);
    }
}
