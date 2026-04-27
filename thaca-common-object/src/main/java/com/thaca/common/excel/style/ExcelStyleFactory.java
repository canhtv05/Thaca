package com.thaca.common.excel.style;

import org.apache.poi.ss.usermodel.*;

/**
 * Factory for creating consistent, professional Excel cell styles.
 * All styles are cached per workbook to avoid duplication.
 */
public final class ExcelStyleFactory {

    private ExcelStyleFactory() {}

    /**
     * Creates the header row style: bold white text on dark blue background.
     */
    public static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);

        style.setFillPattern(FillPatternType.NO_FILL);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        applyThinBorders(style);
        style.setWrapText(true);
        return style;
    }

    /**
     * Creates the style for required header columns: dark red background.
     */
    public static CellStyle createRequiredHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        font.setColor(IndexedColors.RED.getIndex()); // Keep red text for required fields but no bold/bg
        style.setFont(font);

        style.setFillPattern(FillPatternType.NO_FILL);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        applyThinBorders(style);
        style.setWrapText(true);
        return style;
    }

    /**
     * Creates the default data cell style.
     */
    public static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        style.setFont(font);

        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorders(style);
        return style;
    }

    /**
     * Creates a date cell style with the specified format.
     */
    public static CellStyle createDateStyle(Workbook workbook, String dateFormat) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat(dateFormat));
        return style;
    }

    /**
     * Creates a number cell style.
     */
    public static CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    /**
     * Creates the style for alternating rows (light gray background).
     */
    public static CellStyle createAlternateRowStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static void applyThinBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
    }
}
