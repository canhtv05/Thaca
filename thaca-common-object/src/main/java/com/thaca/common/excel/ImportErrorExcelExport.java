package com.thaca.common.excel;

import com.thaca.common.dtos.internal.ImportResponseDTO;
import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelDataType;
import com.thaca.common.excel.schema.ExcelSchema;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ImportErrorExcelExport {

    private ImportErrorExcelExport() {}

    public static ExcelSchema buildSchema(boolean vietnamese) {
        return ExcelSchema.builder()
            .sheetName(vietnamese ? "Xuất lỗi" : "Export error")
            .headerRowIndex(0)
            .dataStartRowIndex(1)
            .strictHeader(true)
            .failFast(false)
            .maxRows(100_000)
            .addColumn(ExcelColumn.builder("row", vietnamese ? "Dòng" : "Row").dataType(ExcelDataType.NUMBER).build())
            .addColumn(
                ExcelColumn.builder("column", vietnamese ? "Cột (tiêu đề)" : "Column (header)").maxLength(200).build()
            )
            .addColumn(ExcelColumn.builder("columnKey", vietnamese ? "Khóa cột" : "Column key").maxLength(100).build())
            .addColumn(ExcelColumn.builder("value", vietnamese ? "Giá trị" : "Value").maxLength(500).build())
            .addColumn(
                ExcelColumn.builder("message", vietnamese ? "Thông báo lỗi" : "Error message").maxLength(2000).build()
            )
            .build();
    }

    public static List<Map<String, Object>> toRows(ImportResponseDTO importResult) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (importResult == null || importResult.getErrors() == null) {
            return rows;
        }
        for (ImportResponseDTO.ImportErrorDTO e : importResult.getErrors()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("row", e.getRow());
            row.put("column", e.getColumn());
            row.put("columnKey", e.getColumnKey());
            row.put("value", e.getValue());
            row.put("message", e.getMessage());
            rows.add(row);
        }
        return rows;
    }

    public static byte[] export(ImportResponseDTO importResult, boolean vietnamese) throws IOException {
        return ExcelEngine.exportData(buildSchema(vietnamese), toRows(importResult));
    }
}
