package com.thaca.auth.validators.rules;

import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.validators.core.ValidateRule;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import org.apache.commons.lang3.StringUtils;

public class EmailRule<T extends UserDTO> implements ValidateRule<T> {

    @Override
    public void validate(T input) {
        if (StringUtils.isEmpty(input.getEmail())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (input.getEmail().trim().length() > 255) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }
        if (!input.getEmail().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }
        if (input.getEmail().trim().contains("+")) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }
    }
}
