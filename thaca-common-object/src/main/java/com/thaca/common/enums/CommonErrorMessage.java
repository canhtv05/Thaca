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
    ),

    DUPLICATE_TRANS_ID(
        "DUPLICATE.TRANS.ID",
        "Yêu cầu đã được gửi trước đó",
        "Giao dịch này đã được xử lý hoặc đang được xử lý. Vui lòng không gửi lại yêu cầu.",
        "Duplicate request",
        "This transaction has already been processed or is currently being processed. Please do not resubmit the " +
            "request."
    ),

    DATA_WAS_MODIFIED_BY_ANOTHER_USER(
        "DATA.WAS.MODIFIED.BY.ANOTHER.USER",
        "Dữ liệu đã bị thay đổi",
        "Dữ liệu đã được chỉnh sửa bởi người khác. Vui lòng tải lại trang và thử lại.",
        "Data was modified",
        "This data has been modified by another user. Please reload and try again."
    ),
    EXCEL_INVALID(
        "EXCEL.INVALID",
        "File Excel không hợp lệ",
        "File Excel không hợp lệ",
        "Excel invalid",
        "Excel invalid"
    ),
    CODE_INVALID(
        "CODE.INVALID",
        "Mã không hợp lệ",
        "Mã không được chứa ký tự đặc biệt. Chỉ được chứa ký tự A-Z, a-z, 0-9",
        "Code invalid",
        "Code invalid. It must contain only letters A-Z, a-z, 0-9"
    ),
    EMAIL_INVALID("EMAIL.INVALID", "Email không hợp lệ", "Email không hợp lệ", "Email invalid", "Email invalid"),
    USERNAME_INVALID(
        "USERNAME.INVALID",
        "Tên đăng nhập không hợp lệ",
        "Tên đăng nhập phải từ 4-50 ký tự, chỉ gồm chữ thường, số và ._-",
        "Username invalid",
        "Username must be 4-50 characters, lowercase letters, numbers and ._- only"
    ),
    FULLNAME_INVALID(
        "FULLNAME.INVALID",
        "Họ tên không hợp lệ",
        "Họ tên phải từ 2-100 ký tự và không chứa ký tự đặc biệt",
        "Fullname invalid",
        "Fullname must be 2-100 characters and not contain special characters"
    ),
    PASSWORD_INVALID(
        "PASSWORD.INVALID",
        "Mật khẩu không chính xác",
        "Mật khẩu không chính xác",
        "Password invalid",
        "Password invalid"
    ),
    PASSWORD_TOO_WEAK(
        "PASSWORD.TOO.WEAK",
        "Mật khẩu quá yếu",
        "Mật khẩu phải chứa ít nhất 1 chữ và 1 số",
        "Password too weak",
        "Password must contain at least 1 letter and 1 number"
    ),
    USERNAME_LENGTH_INVALID(
        "USERNAME.LENGTH.INVALID",
        "Độ dài tên đăng nhập không hợp lệ",
        "Tên đăng nhập phải từ 4 đến 50 ký tự",
        "Username length invalid",
        "Username must be between 4 and 50 characters"
    ),
    PASSWORD_LENGTH_INVALID(
        "PASSWORD.LENGTH.INVALID",
        "Độ dài mật khẩu không hợp lệ",
        "Mật khẩu phải từ 6 đến 100 ký tự",
        "Password length invalid",
        "Password must be between 6 and 100 characters"
    ),
    EMAIL_LENGTH_INVALID(
        "EMAIL.LENGTH.INVALID",
        "Độ dài email không hợp lệ",
        "Email không được vượt quá 255 ký tự",
        "Email length invalid",
        "Email must not exceed 255 characters"
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
