package com.thaca.auth.enums;

import com.thaca.common.validations.ErrorMessageRule;
import lombok.Getter;

@Getter
public enum ErrorMessage implements ErrorMessageRule {
    REFRESH_TOKEN_INVALID("40100", "Token hết hạn", "Token hết hạn", "Refresh token expired", "Refresh token expired"),
    PASSWORD_INVALID("40101", "Mật khẩu không hợp lệ", "Mật khẩu không hợp lệ", "Password invalid", "Password invalid"),
    USER_NOT_FOUND(
        "40102",
        "Tài khoản không tồn tại",
        "Tài khoản không tồn tại",
        "Account not found",
        "Account not found"
    ),
    USER_LOCKED("40103", "Tài khoản đã bị khóa", "Tài khoản đã bị khóa", "Account locked", "Account locked"),
    USER_NOT_ACTIVATED(
        "40104",
        "Tài khoản chưa được kích hoạt",
        "Tài khoản chưa được kích hoạt",
        "Account not activated",
        "Account not activated"
    ),
    USERNAME_ALREADY_EXITS(
        "40105",
        "Tài khoản đã tồn tại",
        "Tài khoản đã tồn tại",
        "Account already exists",
        "Account already exists"
    ),
    EMAIL_INVALID("40106", "Email không hợp lệ", "Email không hợp lệ", "Email invalid", "Email invalid"),
    EMAIL_ALREADY_EXITS(
        "40107",
        "Email đã tồn tại",
        "Email đã tồn tại",
        "Email already exists",
        "Email already exists"
    ),
    FORGET_PASSWORD_OTP_INVALID(
        "40108",
        "Mã OTP không hợp lệ",
        "Mã OTP không hợp lệ",
        "Forgot password OTP invalid",
        "Forgot password OTP invalid"
    ),
    FORGET_PASSWORD_OTP_NOT_SENT_OR_EXPIRED(
        "40109",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Forgot password OTP not sent or expired",
        "Forgot password OTP not sent or expired"
    ),
    CHANGE_PASSWORD_OTP_INVALID(
        "40110",
        "Mã OTP không hợp lệ",
        "Mã OTP không hợp lệ",
        "Change password OTP invalid",
        "Change password OTP invalid"
    ),
    CHANGE_PASSWORD_OTP_NOT_SENT_OR_EXPIRED(
        "40111",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Change password OTP not sent or expired",
        "Change password OTP not sent or expired"
    ),
    UPDATE_USER_PROFILE_OTP_INVALID(
        "40112",
        "Mã OTP không hợp lệ",
        "Mã OTP không hợp lệ",
        "Update user profile OTP invalid",
        "Update user profile OTP invalid"
    ),
    UPDATE_USER_PROFILE_OTP_NOT_SENT_OR_EXPIRED(
        "40113",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Mã OTP không được gửi hoặc đã hết hạn",
        "Update user profile OTP not sent or expired",
        "Update user profile OTP not sent or expired"
    ),
    FORGET_PASSWORD_OTP_ALREADY_SENT(
        "40114",
        "Mã OTP đã được gửi",
        "Mã OTP đã được gửi",
        "Forgot password OTP already sent",
        "Forgot password OTP already sent"
    ),
    CHANNEL_INVALID("40115", "Channel không hợp lệ", "Channel không hợp lệ", "Channel invalid", "Channel invalid"),
    ACCESS_TOKEN_INVALID(
        "40116",
        "Access token không hợp lệ",
        "Access token không hợp lệ",
        "Access token invalid",
        "Access token invalid"
    ),
    TOKEN_PAIR_INVALID(
        "40117",
        "Token pair không hợp lệ",
        "Token pair không hợp lệ",
        "Token pair invalid",
        "Token pair invalid"
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
