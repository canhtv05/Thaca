package com.thaca.cms.controllers.external;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.excel.exception.ExcelFormatException;
import com.thaca.common.excel.exception.ExcelSecurityException;
import com.thaca.common.excel.exception.ExcelValidationException;
import com.thaca.common.excel.result.ImportResult;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Example controller to test all Excel Engine features.
 * Integrated with the Thaca Framework using @FwRequest and FwApiProcess.
 * All endpoints use POST as per project standards.
 */
@Slf4j
@RestController
@RequestMapping("/cms/example/excel")
@RequiredArgsConstructor
public class ExampleController {

    private final FwApiProcess process;

    /**
     * Download the Employee import template via POST.
     */
    @PostMapping("/template")
    @FwRequest(name = ServiceMethod.CMS_EXCEL_GENERATE_TEMPLATE, type = RequestType.PUBLIC)
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        byte[] content = process.process(null);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=thaca-employees-template.xlsx");
        response.getOutputStream().write(content);
        response.flushBuffer();
    }

    /**
     * Import employees from an uploaded Excel file.
     */
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @FwRequest(name = ServiceMethod.CMS_EXCEL_IMPORT, type = RequestType.PUBLIC)
    public ResponseEntity<Map<String, Object>> importFile(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult<Map<String, Object>> result = process.process(file);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("totalRows", result.getTotalRows());
            response.put("successCount", result.getSuccessCount());
            response.put("errorCount", result.getErrorCount());
            response.put("hasErrors", result.hasErrors());

            if (result.hasErrors()) {
                response.put(
                    "errors",
                    result
                        .getErrors()
                        .stream()
                        .map(e -> {
                            Map<String, Object> errorMap = new LinkedHashMap<>();
                            errorMap.put("row", e.getRowIndex() + 1);
                            errorMap.put("column", e.getColumnHeader());
                            errorMap.put("columnKey", e.getColumnKey());
                            errorMap.put("message", e.getErrorMessage());
                            errorMap.put("value", e.getValue() != null ? e.getValue().toString() : null);
                            return errorMap;
                        })
                        .toList()
                );
            }

            if (!result.getSuccessRows().isEmpty()) {
                // Show first 5 rows as preview
                response.put("preview", result.getSuccessRows().stream().limit(5).toList());
            }

            HttpStatus status = result.hasErrors() ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);
        } catch (ExcelSecurityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("error", "SECURITY_ERROR", "message", e.getMessage())
            );
        } catch (ExcelValidationException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error", "VALIDATION_ERROR");
            response.put("message", e.getMessage());
            if (e.getImportResult() != null) {
                response.put("totalRows", e.getImportResult().getTotalRows());
                response.put(
                    "errors",
                    e
                        .getImportResult()
                        .getErrors()
                        .stream()
                        .map(err ->
                            Map.of(
                                "row",
                                err.getRowIndex() + 1,
                                "column",
                                err.getColumnHeader(),
                                "message",
                                err.getErrorMessage()
                            )
                        )
                        .toList()
                );
            }
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        } catch (ExcelFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("error", "FORMAT_ERROR", "message", e.getMessage())
            );
        }
    }

    /**
     * Export sample employee data (50 records) via POST.
     */
    @PostMapping("/export")
    @FwRequest(name = ServiceMethod.CMS_EXCEL_EXPORT, type = RequestType.PUBLIC)
    public void exportData(HttpServletResponse response) throws IOException {
        byte[] content = process.process(null);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=thaca-employees-export.xlsx");
        response.getOutputStream().write(content);
        response.flushBuffer();
    }
}
