package com.thaca.framework.core.validations.rules;

import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.validations.ValidateRule;
import org.apache.commons.lang3.StringUtils;

public class UsernameRule<T extends UserDTO> implements ValidateRule<T> {

    @Override
    public void validate(T input) {
        if (StringUtils.isBlank(input.getUsername())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (input.getUsername().trim().length() < 4 || input.getUsername().trim().length() > 50) {
            throw new FwException(CommonErrorMessage.USERNAME_LENGTH_INVALID);
        }
        if (!input.getUsername().matches("^[a-z0-9._-]+$")) {
            throw new FwException(CommonErrorMessage.USERNAME_INVALID);
        }
    }
}
