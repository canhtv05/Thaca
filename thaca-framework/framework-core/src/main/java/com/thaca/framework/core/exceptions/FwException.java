package com.thaca.framework.core.exceptions;

import com.thaca.common.validations.ErrorMessageRule;
import com.thaca.framework.core.utils.CommonUtils;
import java.util.Map;
import lombok.Getter;

@Getter
public class FwException extends RuntimeException {

    private final ErrorMessageRule errorMessage;

    @Getter
    private Map<String, Object> data;

    public FwException(ErrorMessageRule errorMessage) {
        super(errorMessage.messageVi());
        this.errorMessage = errorMessage;
    }

    public FwException(ErrorMessageRule errorMessage, String message) {
        super(message);
        this.errorMessage = errorMessage;
    }

    public FwException(ErrorMessageRule errorMessage, String messageVi, String messageEn) {
        super(messageEn);
        this.errorMessage = errorMessage;
    }

    public FwException(ErrorMessageRule errorMessage, Map<String, Object> data) {
        super(CommonUtils.formatMessage(errorMessage.messageVi(), data));
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public FwException(ErrorMessageRule errorMessage, String message, Map<String, Object> data) {
        super(CommonUtils.formatMessage(message, data));
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public FwException(ErrorMessageRule errorMessage, String messageVi, String messageEn, Map<String, Object> data) {
        super(CommonUtils.formatMessage(messageVi, data));
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public static FwException error(ErrorMessageRule errorMessage) {
        return new FwException(errorMessage);
    }
}
