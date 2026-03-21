package com.thaca.common.enums;

import com.thaca.common.validations.ErrorMessageRule;
import lombok.Getter;

@Getter
public enum CommonErrorMessage implements ErrorMessageRule {
    INTERNAL_SERVER_ERROR(
        "50001",
        "Lỗi hệ thống",
        "Đã xảy ra lỗi không xác định",
        "System error",
        "Unknown internal server error"
    ),

    BAD_REQUEST("40001", "Yêu cầu không hợp lệ", "Dữ liệu gửi lên không hợp lệ", "Bad request", "Invalid request data"),

    INVALID_REQUEST_BODY(
        "40002",
        "Dữ liệu request không hợp lệ",
        "Body request không đúng định dạng hoặc không thể đọc",
        "Invalid request body",
        "Request body is malformed or unreadable"
    ),

    UNAUTHORIZED(
        "40101",
        "Chưa xác thực",
        "Bạn chưa đăng nhập hoặc token không hợp lệ",
        "Unauthorized",
        "Authentication required or invalid token"
    ),

    FORBIDDEN(
        "40301",
        "Không có quyền",
        "Bạn không có quyền truy cập tài nguyên này",
        "Forbidden",
        "You do not have permission to access this resource"
    ),

    NOT_FOUND(
        "40401",
        "Không tìm thấy tài nguyên",
        "Tài nguyên không tồn tại",
        "Not found",
        "Requested resource not found"
    ),

    METHOD_NOT_ALLOWED(
        "40501",
        "Phương thức không hợp lệ",
        "Phương thức HTTP không được hỗ trợ",
        "Method not allowed",
        "HTTP method is not supported"
    ),

    CONFLICT(
        "40901",
        "Xung đột dữ liệu",
        "Dữ liệu đã tồn tại hoặc gây xung đột",
        "Conflict",
        "Data conflict or already exists"
    ),

    VALIDATION_FAILED(
        "42201",
        "Dữ liệu không hợp lệ",
        "Dữ liệu không thỏa mãn điều kiện kiểm tra",
        "Validation failed",
        "Request validation failed"
    ),

    BINDING_ERROR(
        "42203",
        "Lỗi ánh xạ dữ liệu",
        "Không thể ánh xạ dữ liệu request",
        "Binding error",
        "Failed to bind request parameters"
    ),

    SERVICE_UNAVAILABLE(
        "50301",
        "Dịch vụ tạm thời không khả dụng",
        "Hệ thống đang bảo trì hoặc quá tải",
        "Service unavailable",
        "Service temporarily unavailable"
    ),

    USER_SESSION_NOT_FOUND(
        "40102",
        "Không tìm thấy phiên đăng nhập",
        "Không tìm thấy phiên đăng nhập của người dùng",
        "Session not found",
        "User authentication session not found"
    ),

    IMPORT_EXCEL_ERROR("42204", "Lỗi import excel", "Lỗi import excel", "Import excel error", "Import excel error");

    private final String code;
    private final String titleVi;
    private final String messageVi;
    private final String titleEn;
    private final String messageEn;

    CommonErrorMessage(String code, String titleVi, String messageVi, String titleEn, String messageEn) {
        this.code = code;
        this.titleVi = titleVi;
        this.messageVi = messageVi;
        this.titleEn = titleEn;
        this.messageEn = messageEn;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String titleVi() {
        return titleVi;
    }

    @Override
    public String messageVi() {
        return messageVi;
    }

    @Override
    public String titleEn() {
        return titleEn;
    }

    @Override
    public String messageEn() {
        return messageEn;
    }
}
