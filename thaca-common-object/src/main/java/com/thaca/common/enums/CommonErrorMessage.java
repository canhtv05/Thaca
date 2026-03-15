package com.thaca.common.enums;

import com.thaca.common.validations.ErrorMessageRule;
import lombok.Getter;

@Getter
public enum CommonErrorMessage implements ErrorMessageRule {

    INTERNAL_SERVER(
            "500",
            "Lỗi hệ thống",
            "Đã xảy ra lỗi không xác định",
            "System error",
            "Unknown error occurred"
    ),
    BAD_REQUEST(
            "400",
            "Yêu cầu không hợp lệ",
            "Dữ liệu gửi lên không hợp lệ",
            "Bad request",
            "Invalid request data"
    ),
    UNAUTHORIZED(
            "401",
            "Chưa xác thực",
            "Bạn chưa đăng nhập hoặc token không hợp lệ",
            "Unauthorized",
            "Authentication required or invalid token"
    ),
    FORBIDDEN(
            "403",
            "Không có quyền",
            "Bạn không có quyền truy cập tài nguyên này",
            "Forbidden",
            "You do not have permission to access this resource"
    ),
    NOT_FOUND(
            "404",
            "Không tìm thấy dữ liệu",
            "Tài nguyên không tồn tại",
            "Not found",
            "Resource not found"
    ),
    METHOD_NOT_ALLOWED(
            "405",
            "Phương thức không hợp lệ",
            "Phương thức HTTP không được hỗ trợ",
            "Method not allowed",
            "HTTP method is not supported"
    ),
    CONFLICT(
            "409",
            "Xung đột dữ liệu",
            "Dữ liệu đã tồn tại hoặc gây xung đột",
            "Conflict",
            "Data conflict or already exists"
    ),
    VALIDATION_FAILED(
            "422",
            "Dữ liệu không hợp lệ",
            "Dữ liệu không thỏa mãn điều kiện kiểm tra",
            "Validation failed",
            "Request validation failed"
    ),
    SERVICE_UNAVAILABLE(
            "503",
            "Dịch vụ tạm thời không khả dụng",
            "Hệ thống đang bảo trì hoặc quá tải",
            "Service unavailable",
            "Service temporarily unavailable"
    );

    private final String code;
    private final String titleVi;
    private final String messageVi;
    private final String titleEn;
    private final String messageEn;

    CommonErrorMessage(
            String code,
            String titleVi,
            String messageVi,
            String titleEn,
            String messageEn
    ) {
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