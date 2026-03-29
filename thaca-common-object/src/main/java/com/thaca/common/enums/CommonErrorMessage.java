package com.thaca.common.enums;

import com.thaca.common.validations.ErrorMessageRule;
import lombok.Getter;

@Getter
public enum CommonErrorMessage implements ErrorMessageRule {
    INTERNAL_SERVER_ERROR(
        "INTERNAL.SERVER.ERROR",
        "Lỗi hệ thống",
        "Đã xảy ra lỗi không xác định",
        "System error",
        "Unknown internal server error"
    ),

    BAD_REQUEST(
        "BAD.REQUEST",
        "Yêu cầu không hợp lệ",
        "Dữ liệu gửi lên không hợp lệ",
        "Bad request",
        "Invalid request data"
    ),

    INVALID_REQUEST_BODY(
        "INVALID.REQUEST.BODY",
        "Dữ liệu request không hợp lệ",
        "Body request không đúng định dạng hoặc không thể đọc",
        "Invalid request body",
        "Request body is malformed or unreadable"
    ),

    UNAUTHORIZED(
        "UNAUTHORIZED",
        "Chưa xác thực",
        "Bạn chưa đăng nhập hoặc token không hợp lệ",
        "Unauthorized",
        "Authentication required or invalid token"
    ),

    FORBIDDEN(
        "FORBIDDEN",
        "Không có quyền",
        "Bạn không có quyền truy cập tài nguyên này",
        "Forbidden",
        "You do not have permission to access this resource"
    ),

    NOT_FOUND(
        "NOT.FOUND",
        "Không tìm thấy tài nguyên",
        "Tài nguyên không tồn tại",
        "Not found",
        "Requested resource not found"
    ),

    METHOD_NOT_ALLOWED(
        "METHOD.NOT.ALLOWED",
        "Phương thức không hợp lệ",
        "Phương thức HTTP không được hỗ trợ",
        "Method not allowed",
        "HTTP method is not supported"
    ),

    CONFLICT(
        "CONFLICT",
        "Xung đột dữ liệu",
        "Dữ liệu đã tồn tại hoặc gây xung đột",
        "Conflict",
        "Data conflict or already exists"
    ),

    VALIDATION_FAILED(
        "VALIDATION.FAILED",
        "Dữ liệu không hợp lệ",
        "Dữ liệu không thỏa mãn điều kiện kiểm tra",
        "Validation failed",
        "Request validation failed"
    ),

    BINDING_ERROR(
        "BINDING.ERROR",
        "Lỗi ánh xạ dữ liệu",
        "Không thể ánh xạ dữ liệu request",
        "Binding error",
        "Failed to bind request parameters"
    ),

    SERVICE_UNAVAILABLE(
        "SERVICE.UNAVAILABLE",
        "Dịch vụ tạm thời không khả dụng",
        "Hệ thống đang bảo trì hoặc quá tải",
        "Service unavailable",
        "Service temporarily unavailable"
    ),

    USER_SESSION_NOT_FOUND(
        "USER.SESSION.NOT.FOUND",
        "Không tìm thấy phiên đăng nhập",
        "Không tìm thấy phiên đăng nhập của người dùng",
        "Session not found",
        "User authentication session not found"
    ),

    REQUEST_INVALID_PARAMS(
        "REQUEST.INVALID.PARAMS",
        "Dữ liệu request không hợp lệ",
        "Dữ liệu request không hợp lệ",
        "Request invalid params",
        "Request invalid params"
    ),

    CHANNEL_INVALID(
        "CHANNEL.INVALID",
        "Dữ liệu request không hợp lệ",
        "Channel không hợp lệ",
        "Request invalid params",
        "Channel invalid params"
    );

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
