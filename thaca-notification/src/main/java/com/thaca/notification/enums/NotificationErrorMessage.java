package com.thaca.notification.enums;

import com.thaca.common.validations.ErrorMessageRule;
import lombok.Getter;

@Getter
public enum NotificationErrorMessage implements ErrorMessageRule {
    MAIL_CONFIG_NOT_FOUND(
        "MAIL.CONFIG.NOT_FOUND",
        "Cấu hình mail không tồn tại",
        "Không tìm thấy cấu hình gửi mail cho ngữ cảnh hiện tại.",
        "Mail config not found",
        "Mail configuration not found for the current context."
    ),
    MAIL_CONFIG_MISSING(
        "MAIL.CONFIG.MISSING",
        "Thiếu cấu hình gửi mail",
        "Hệ thống chưa được cấu hình SMTP để gửi mail.",
        "Mail config missing",
        "System is missing SMTP configuration to send emails."
    );

    private final String code;
    private final String titleVi;
    private final String messageVi;
    private final String titleEn;
    private final String messageEn;

    NotificationErrorMessage(String code, String titleVi, String messageVi, String titleEn, String messageEn) {
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
