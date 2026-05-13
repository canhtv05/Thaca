package com.thaca.framework.core.validations.rules;

import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.validations.ValidateRule;
import org.apache.commons.lang3.StringUtils;

public class PasswordRule<T extends UserDTO> implements ValidateRule<T> {

    @Override
    public void validate(T input) {
        if (StringUtils.isBlank(input.getPassword())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (input.getPassword().trim().length() < 6 || input.getPassword().trim().length() > 100) {
            throw new FwException(CommonErrorMessage.PASSWORD_LENGTH_INVALID);
        }
        if (!input.getPassword().trim().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&._-]).+$")) {
            throw new FwException(CommonErrorMessage.PASSWORD_TOO_WEAK);
        }
    }
}
