package com.thaca.auth.validators.rules;

import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.validators.core.ValidateRule;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import org.apache.commons.lang3.StringUtils;

public class UsernameRule<T extends UserDTO> implements ValidateRule<T> {

    @Override
    public void validate(T input) {
        if (StringUtils.isEmpty(input.getUsername())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (input.getUsername().trim().length() < 4 || input.getUsername().trim().length() > 50) {
            throw new FwException(ErrorMessage.USERNAME_LENGTH_INVALID);
        }
        if (!input.getUsername().matches("^[a-z0-9._-]+$")) {
            throw new FwException(ErrorMessage.USERNAME_INVALID);
        }
    }
}
