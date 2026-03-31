package com.thaca.auth.validators.rules;

import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.validators.core.ValidateRule;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import org.apache.commons.lang3.StringUtils;

public class PasswordRule<T extends UserDTO> implements ValidateRule<T> {

    @Override
    public void validate(T input) {
        if (StringUtils.isEmpty(input.getPassword())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (input.getPassword().trim().length() < 6 || input.getPassword().trim().length() > 100) {
            throw new FwException(ErrorMessage.PASSWORD_LENGTH_INVALID);
        }
        if (!input.getPassword().trim().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&._-]).+$")) {
            throw new FwException(ErrorMessage.PASSWORD_TOO_WEAK);
        }
    }
}
