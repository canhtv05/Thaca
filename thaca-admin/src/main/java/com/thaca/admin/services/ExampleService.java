package com.thaca.admin.services;

import com.thaca.admin.constants.ServiceMethod;
import com.thaca.common.excel.ExcelEngine;
import com.thaca.common.excel.result.ImportResult;
import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelDataType;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Example service demonstrating all Excel Engine features.
 * This is a reference implementation for testing — use as a blueprint for real
 * business services.
 */
@Service
public class ExampleService {

    /**
     * Schema for "Employee" import/export — demonstrates every feature of
     * ExcelColumn.
     */
    private final ExcelSchema employeeSchema = ExcelSchema.builder()
        .sheetName("Employees")
        .headerRowIndex(0)
        .dataStartRowIndex(1)
        .strictHeader(true)
        .failFast(false)
        .maxRows(50000)
        // 1) Required string with maxLength
        .addColumn(
            ExcelColumn.builder("employeeCode", "Employee Code")
                .required()
                .maxLength(20)
                .comment("Unique employee code, e.g. EMP-001")
                .build()
        )
        // 2) Required string with custom validator (email format)
        .addColumn(ExcelColumn.builder("fullName", "Full Name").required().maxLength(100).build())
        // 3) Email with custom validator
        .addColumn(
            ExcelColumn.builder("email", "Email")
                .required()
                .maxLength(150)
                .customValidator((val, ctx) -> {
                    if (val == null) return null;
                    String email = val.toString();
                    if (!email.contains("@") || !email.contains(".")) {
                        return "Invalid email format";
                    }
                    return null;
                })
                .comment("Valid email address, e.g. user@company.com")
                .build()
        )
        // 4) Number with range
        .addColumn(
            ExcelColumn.builder("age", "Age")
                .dataType(ExcelDataType.NUMBER)
                .range(18, 65)
                .comment("Must be between 18 and 65")
                .build()
        )
        // 5) Dropdown with allowedValues
        .addColumn(
            ExcelColumn.builder("department", "Department")
                .required()
                .allowedValues("IT", "HR", "FINANCE", "MARKETING", "OPERATIONS")
                .comment("Select from dropdown list")
                .build()
        )
        // 6) Dropdown with allowedValues
        .addColumn(
            ExcelColumn.builder("position", "Position")
                .allowedValues("INTERN", "JUNIOR", "SENIOR", "LEAD", "MANAGER", "DIRECTOR")
                .build()
        )
        // 7) Number with range (salary)
        .addColumn(
            ExcelColumn.builder("salary", "Salary")
                .dataType(ExcelDataType.NUMBER)
                .minValue(0)
                .maxValue(1000000000)
                .comment("Monthly salary in VND")
                .build()
        )
        // 8) Date with custom format
        .addColumn(
            ExcelColumn.builder("joinDate", "Join Date")
                .dataType(ExcelDataType.DATE)
                .dateFormat("dd/MM/yyyy")
                .comment("Format: dd/MM/yyyy, e.g. 15/03/2024")
                .build()
        )
        // 9) Boolean
        .addColumn(
            ExcelColumn.builder("active", "Active")
                .dataType(ExcelDataType.BOOLEAN)
                .comment("true/false, yes/no, 1/0")
                .build()
        )
        // 10) String with valueMapper (transform input)
        .addColumn(
            ExcelColumn.builder("phoneNumber", "Phone Number")
                .maxLength(15)
                .valueMapper(raw -> {
                    // Remove spaces and dashes, normalize phone format
                    String cleaned = raw.replaceAll("[\\s\\-()]", "");
                    if (cleaned.startsWith("0")) {
                        cleaned = "+84" + cleaned.substring(1);
                    }
                    return cleaned;
                })
                .comment("Phone number, will be normalized to +84 format")
                .build()
        )
        // 11) Cross-field validation example
        .addColumn(
            ExcelColumn.builder("managerCode", "Manager Code")
                .maxLength(20)
                .customValidator((val, ctx) -> {
                    String position = (String) ctx.getValue("position");
                    if ("DIRECTOR".equals(position) && val != null && !val.toString().isBlank()) {
                        return "Directors should not have a manager";
                    }
                    if ("INTERN".equals(position) && (val == null || val.toString().isBlank())) {
                        return "Interns must have a manager assigned";
                    }
                    return null;
                })
                .comment("Manager's employee code. Required for interns, not allowed for directors.")
                .build()
        )
        .build();

    /**
     * Returns the employee schema (for reuse in controller).
     */
    public ExcelSchema getEmployeeSchema() {
        return employeeSchema;
    }

    /**
     * Generate template for download.
     */
    @FwMode(name = ServiceMethod.ADMIN_EXCEL_GENERATE_TEMPLATE, type = ModeType.HANDLE)
    public byte[] generateTemplate(Object ignored) throws IOException {
        return ExcelEngine.generateTemplate(employeeSchema);
    }

    /**
     * Import employees from uploaded file.
     */
    @FwMode(name = ServiceMethod.ADMIN_EXCEL_IMPORT, type = ModeType.HANDLE)
    public ImportResult<Map<String, Object>> importEmployees(MultipartFile file) {
        return ExcelEngine.importFile(file, employeeSchema);
    }

    /**
     * Export sample employee data (for testing export feature).
     */
    @FwMode(name = ServiceMethod.ADMIN_EXCEL_EXPORT, type = ModeType.HANDLE)
    public byte[] exportSampleData(Object ignored) throws IOException {
        List<Map<String, Object>> sampleData = generateSampleData();
        return ExcelEngine.exportData(employeeSchema, sampleData);
    }

    /**
     * Generates 50 sample employee records for testing.
     */
    private List<Map<String, Object>> generateSampleData() {
        String[] departments = { "IT", "HR", "FINANCE", "MARKETING", "OPERATIONS" };
        String[] positions = { "INTERN", "JUNIOR", "SENIOR", "LEAD", "MANAGER", "DIRECTOR" };

        List<Map<String, Object>> data = new ArrayList<>();

        for (int i = 1; i <= 50; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("employeeCode", String.format("EMP-%03d", i));
            row.put("fullName", "Employee " + i);
            row.put("email", "employee" + i + "@thaca.com");
            row.put("age", 20 + (i % 40));
            row.put("department", departments[i % departments.length]);
            row.put("position", positions[i % positions.length]);
            row.put("salary", 10000000 + (i * 500000));
            row.put("joinDate", LocalDate.of(2020, 1, 1).plusDays(i * 15));
            row.put("active", i % 5 != 0); // Every 5th is inactive
            row.put("phoneNumber", "+8490" + String.format("%07d", i));
            row.put("managerCode", i > 5 ? String.format("EMP-%03d", (i % 5) + 1) : "");
            data.add(row);
        }

        return data;
    }
}
