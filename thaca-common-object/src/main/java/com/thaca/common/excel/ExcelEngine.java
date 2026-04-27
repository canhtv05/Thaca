package com.thaca.common.excel;

import com.thaca.common.excel.reader.ExcelReader;
import com.thaca.common.excel.result.ImportResult;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.common.excel.writer.ExcelExporter;
import com.thaca.common.excel.writer.ExcelTemplateBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.web.multipart.MultipartFile;

/**
 * Main facade for the Thaca Excel Engine.
 * <p>
 * Provides a single entry point for:
 * - Template generation
 * - File import with validation
 * - Data export with streaming
 * <p>
 * All operations are schema-driven — no annotations, no reflection.
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * ExcelSchema schema = ExcelSchema.builder()
 *     .sheetName("Users")
 *     .addColumn(ExcelColumn.builder("username", "Username").required().maxLength(50).build())
 *     .addColumn(ExcelColumn.builder("email", "Email").required().maxLength(100).build())
 *     .addColumn(ExcelColumn.builder("age", "Age").dataType(ExcelDataType.NUMBER).range(1, 150).build())
 *     .addColumn(ExcelColumn.builder("role", "Role").allowedValues("ADMIN", "USER", "MANAGER").build())
 *     .build();
 *
 * // Generate template
 * byte[] template = ExcelEngine.generateTemplate(schema);
 *
 * // Import from upload
 * ImportResult<Map<String, Object>> result = ExcelEngine.importFile(multipartFile, schema);
 *
 * // Export data
 * ExcelEngine.exportData(schema, dataList, response.getOutputStream());
 * }</pre>
 */
public final class ExcelEngine {

    private ExcelEngine() {}

    // ═══════════════════════════════════════════════════════════
    //  TEMPLATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Generates an Excel template as a byte array.
     */
    public static byte[] generateTemplate(ExcelSchema schema) throws IOException {
        return ExcelTemplateBuilder.buildAsBytes(schema);
    }

    /**
     * Generates an Excel template and writes to the given output stream.
     */
    public static void generateTemplate(ExcelSchema schema, OutputStream out) throws IOException {
        ExcelTemplateBuilder.build(schema, out);
    }

    // ═══════════════════════════════════════════════════════════
    //  IMPORT
    // ═══════════════════════════════════════════════════════════

    /**
     * Imports from a MultipartFile with full security + validation.
     *
     * @throws com.thaca.common.excel.exception.ExcelSecurityException   if file fails security checks
     * @throws com.thaca.common.excel.exception.ExcelFormatException     if file structure is invalid
     * @throws com.thaca.common.excel.exception.ExcelValidationException if failFast is true and validation fails
     */
    public static ImportResult<Map<String, Object>> importFile(MultipartFile file, ExcelSchema schema) {
        return ExcelReader.read(file, schema);
    }

    /**
     * Imports from raw bytes with full security + validation.
     */
    public static ImportResult<Map<String, Object>> importBytes(
        byte[] bytes,
        String fileName,
        String contentType,
        ExcelSchema schema
    ) {
        return ExcelReader.read(bytes, fileName, contentType, schema);
    }

    /**
     * Imports from an InputStream (no security checks — use for trusted sources).
     */
    public static ImportResult<Map<String, Object>> importStream(InputStream inputStream, ExcelSchema schema) {
        return ExcelReader.readFromStream(inputStream, schema);
    }

    // ═══════════════════════════════════════════════════════════
    //  EXPORT
    // ═══════════════════════════════════════════════════════════

    /**
     * Exports Map-based data to an output stream using streaming workbook.
     */
    public static void exportData(ExcelSchema schema, List<Map<String, Object>> data, OutputStream out)
        throws IOException {
        ExcelExporter.export(schema, data, out);
    }

    /**
     * Exports Map-based data and returns as byte array.
     */
    public static byte[] exportData(ExcelSchema schema, List<Map<String, Object>> data) throws IOException {
        return ExcelExporter.exportAsBytes(schema, data);
    }

    /**
     * Exports typed objects to an output stream using a row mapper.
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
        ExcelExporter.exportObjects(schema, items, rowMapper, out);
    }

    /**
     * Exports typed objects and returns as byte array.
     */
    public static <T> byte[] exportObjects(
        ExcelSchema schema,
        List<T> items,
        Function<T, Map<String, Object>> rowMapper
    ) throws IOException {
        return ExcelExporter.exportObjectsAsBytes(schema, items, rowMapper);
    }
}
