package com.thaca.framework.core.exceptions;

import com.thaca.common.validations.ErrorMessageRule;
import lombok.Getter;

@Getter
public class FwException extends RuntimeException {

    private final ErrorMessageRule errorMessage;

    public FwException(ErrorMessageRule errorMessage) {
        super(errorMessage.messageEn());
        this.errorMessage = errorMessage;
    }

    public FwException(ErrorMessageRule errorMessage, String message) {
        super(message);
        this.errorMessage = errorMessage;
    }

    public static FwException error(ErrorMessageRule errorMessage) {
        return new FwException(errorMessage);
    }
}