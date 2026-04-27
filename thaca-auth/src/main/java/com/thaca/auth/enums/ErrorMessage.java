package com.thaca.auth.enums;

import com.thaca.common.validations.ErrorMessageRule;
import lombok.Getter;

@Getter
public enum ErrorMessage implements ErrorMessageRule {
    REFRESH_TOKEN_INVALID(
        "REFRESH.TOKEN.INVALID",
        "Refresh token không hợp lệ",
        "Refresh token không hợp lệ",
        "Refresh token invalid",
        "Refresh token invalid"
    ),
    PASSWORD_INVALID(
        "PASSWORD.INVALID",
        "Tên đăng nhập hoặc mật khẩu không chính xác",
        "Tên đăng nhập hoặc mật khẩu không chính xác",
        "Username or password invalid",
        "Username or password invalid"
    ),
    PASSWORD_INVALID_WITH_RETRY(
        "PASSWORD.INVALID.WITH.RETRY",
        "Mật khẩu không chính xác",
        "Mật khẩu không chính xác. Bạn còn {{remainingAttempts}} lần thử.",
        "Incorrect password",
        "Incorrect password. You have {{remainingAttempts}} attempts left."
    ),
    USER_NOT_FOUND(
        "USER.NOT.FOUND",
        "Tài khoản không tồn tại",
        "Tài khoản không tồn tại",
        "Account not found",
        "Account not found"
    ),
    USER_TEMPORARILY_LOCKED(
        "USER.TEMPORARILY.LOCKED",
        "Tài khoản đã bị khóa tạm thời.",
        "Tài khoản của bạn đã bị khóa tạm thời do nhập sai mật khẩu quá nhiều lần.",
        "Account temporarily locked.",
        "Your account has been temporarily locked due to multiple failed login attempts."
    ),
    USER_LOCKED("USER.LOCKED", "Tài khoản đã bị khóa", "Tài khoản đã bị khóa", "Account locked", "Account locked"),
    USER_NOT_ACTIVATED(
        "USER.NOT.ACTIVATED",
        "Tài khoản chưa được kích hoạt",
        "Tài khoản chưa được kích hoạt",
        "Account not activated",
        "Account not activated"
    ),
    USERNAME_ALREADY_EXITS(
        "USERNAME.ALREADY.EXITS",
        "Tài khoản đã tồn tại",
        "Tài khoản đã tồn tại",
        "Account already exists",
        "Account already exists"
    ),
    EMAIL_INVALID("EMAIL.INVALID", "Email không hợp lệ", "Email không hợp lệ", "Email invalid", "Email invalid"),
    EMAIL_ALREADY_EXITS(
        "EMAIL.ALREADY.EXITS",
        "Email đã tồn tại",
        "Email đã tồn tại",
        "Email already exists",
        "Email already exists"
    ),
    FORGET_PASSWORD_OTP_INVALID(
        "FORGET.PASSWORD.OTP.INVALID",
        "Mã OTP không hợp lệ",
        "Mã OTP không hợp lệ",
        "Forgot password OTP invalid",
        "Forgot password OTP invalid"
    ),
    FORGET_PASSWORD_OTP_NOT_SENT_OR_EXPIRED(
        "FORGET.PASSWORD.OTP.NOT.SENT.OR.EXPIRED",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Forgot password OTP not sent or expired",
        "Forgot password OTP not sent or expired"
    ),
    CHANGE_PASSWORD_OTP_INVALID(
        "CHANGE.PASSWORD.OTP.INVALID",
        "Mã OTP không hợp lệ",
        "Mã OTP không hợp lệ",
        "Change password OTP invalid",
        "Change password OTP invalid"
    ),
    CHANGE_PASSWORD_OTP_NOT_SENT_OR_EXPIRED(
        "CHANGE.PASSWORD.OTP.NOT.SENT.OR.EXPIRED",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Change password OTP not sent or expired",
        "Change password OTP not sent or expired"
    ),
    UPDATE_USER_PROFILE_OTP_INVALID(
        "UPDATE.USER.PROFILE.OTP.INVALID",
        "Mã OTP không hợp lệ",
        "Mã OTP không hợp lệ",
        "Update user profile OTP invalid",
        "Update user profile OTP invalid"
    ),
    UPDATE_USER_PROFILE_OTP_NOT_SENT_OR_EXPIRED(
        "UPDATE.USER.PROFILE.OTP.NOT.SENT.OR.EXPIRED",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Update user profile OTP not sent or expired",
        "Update user profile OTP not sent or expired"
    ),
    FORGET_PASSWORD_OTP_ALREADY_SENT(
        "FORGET.PASSWORD.OTP.ALREADY.SENT",
        "Mã OTP đã được gửi",
        "Mã OTP đã được gửi",
        "Forgot password OTP already sent",
        "Forgot password OTP already sent"
    ),
    CHANNEL_INVALID(
        "CHANNEL.INVALID",
        "Channel không hợp lệ",
        "Channel không hợp lệ",
        "Channel invalid",
        "Channel invalid"
    ),
    ACCESS_TOKEN_INVALID(
        "ACCESS.TOKEN.INVALID",
        "Access token không hợp lệ",
        "Access token không hợp lệ",
        "Access token invalid",
        "Access token invalid"
    ),
    TOKEN_PAIR_INVALID(
        "TOKEN.PAIR.INVALID",
        "Token pair không hợp lệ",
        "Token pair không hợp lệ",
        "Token pair invalid",
        "Token pair invalid"
    ),
    EMAIL_NOT_FOUND(
        "EMAIL.NOT.FOUND",
        "Email không tồn tại",
        "Email không tồn tại",
        "Email not found",
        "Email not found"
    ),
    CURRENT_PASSWORD_INVALID(
        "CURRENT.PASSWORD.INVALID",
        "Mật khẩu không chính xác",
        "Mật khẩu hiện tại nhập không chính xác",
        "Current password is incorrect",
        "The current password is incorrect"
    ),
    PASSWORD_NEW_CANNOT_BE_SAME_AS_OLD(
        "PASSWORD.NEW.CANNOT.BE.SAME.AS.OLD",
        "Mật khẩu mới không được trùng với mật khẩu hiện tại",
        "Mật khẩu mới không được trùng với mật khẩu hiện tại",
        "Password new cannot be same as old",
        "Password new cannot be same as old"
    ),
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
    ROLE_INVALID(
        "ROLE.INVALID",
        "Vai trò không hợp lệ",
        "Danh sách vai trò không hợp lệ",
        "Role invalid",
        "Role list is invalid"
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
    ),
    PLAN_INACTIVE_CANNOT_UPDATE(
        "PLAN.INACTIVE",
        "Plan đã bị khóa",
        "Plan đã bị khóa không thể cập nhật. Vui lòng mở khóa plan để cập nhật",
        "Plan is inactive",
        "Plan is inactive cannot update. Please unlock plan to update"
    ),
    PLAN_CODE_INVALID(
        "PLAN.CODE.INVALID",
        "Mã gói không hợp lệ",
        "Mã gói không được chứa ký tự đặc biệt. Chỉ được chứa ký tự A-Z, a-z, 0-9",
        "Plan code invalid",
        "Plan code invalid. It must contain only letters A-Z, a-z, 0-9"
    );

    private final String code;
    private final String titleVi;
    private final String messageVi;
    private final String titleEn;
    private final String messageEn;

    ErrorMessage(String code, String titleVi, String messageVi, String titleEn, String messageEn) {
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
