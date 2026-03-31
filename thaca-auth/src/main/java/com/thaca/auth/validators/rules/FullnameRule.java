package com.thaca.auth.validators.rules;

import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.validators.core.ValidateRule;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import org.apache.commons.lang3.StringUtils;

public class FullnameRule<T extends UserDTO> implements ValidateRule<T> {

    @Override
    public void validate(T input) {
        if (StringUtils.isEmpty(input.getFullname())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (input.getFullname().trim().length() < 2 || input.getFullname().trim().length() > 100) {
            throw new FwException(ErrorMessage.FULLNAME_INVALID);
        }
        if (!input.getFullname().matches("^[\\p{L} .'-]+$")) {
            throw new FwException(ErrorMessage.FULLNAME_INVALID);
        }
    }
}
