package com.thaca.framework.core.exceptions;

import com.thaca.common.validations.ErrorMessageRule;
import java.util.Map;
import lombok.Getter;

@Getter
public class FwException extends RuntimeException {

    private final ErrorMessageRule errorMessage;
    private String customMessageVi;
    private String customMessageEn;

    @Getter
    private Map<String, Object> data;

    public FwException(ErrorMessageRule errorMessage) {
        super(errorMessage.messageEn());
        this.errorMessage = errorMessage;
    }

    public FwException(ErrorMessageRule errorMessage, String message) {
        super(message);
        this.errorMessage = errorMessage;
    }

    public FwException(ErrorMessageRule errorMessage, String messageVi, String messageEn) {
        super(messageEn);
        this.errorMessage = errorMessage;
        this.customMessageVi = messageVi;
        this.customMessageEn = messageEn;
    }

    public FwException(ErrorMessageRule errorMessage, Map<String, Object> data) {
        super(errorMessage.messageEn());
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public FwException(ErrorMessageRule errorMessage, String message, Map<String, Object> data) {
        super(message);
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public FwException(ErrorMessageRule errorMessage, String messageVi, String messageEn, Map<String, Object> data) {
        super(messageEn);
        this.errorMessage = errorMessage;
        this.customMessageVi = messageVi;
        this.customMessageEn = messageEn;
        this.data = data;
    }

    public static FwException error(ErrorMessageRule errorMessage) {
        return new FwException(errorMessage);
    }
}
