package com.thaca.framework.core.validations.rules;

import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.validations.ValidateRule;
import org.apache.commons.lang3.StringUtils;

public class EmailRule<T extends UserDTO> implements ValidateRule<T> {

    @Override
    public void validate(T input) {
        if (StringUtils.isBlank(input.getEmail())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (input.getEmail().trim().length() > 255) {
            throw new FwException(CommonErrorMessage.EMAIL_INVALID);
        }
        if (!input.getEmail().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new FwException(CommonErrorMessage.EMAIL_INVALID);
        }
        if (input.getEmail().trim().contains("+")) {
            throw new FwException(CommonErrorMessage.EMAIL_INVALID);
        }
    }
}
