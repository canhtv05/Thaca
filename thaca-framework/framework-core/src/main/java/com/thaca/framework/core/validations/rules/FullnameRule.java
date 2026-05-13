package com.thaca.framework.core.validations.rules;

import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.validations.ValidateRule;
import org.apache.commons.lang3.StringUtils;

public class FullnameRule<T extends UserDTO> implements ValidateRule<T> {

    @Override
    public void validate(T input) {
        if (StringUtils.isBlank(input.getFullname())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (input.getFullname().trim().length() < 2 || input.getFullname().trim().length() > 100) {
            throw new FwException(CommonErrorMessage.FULLNAME_INVALID);
        }
        if (!input.getFullname().matches("^[\\p{L} .'-]+$")) {
            throw new FwException(CommonErrorMessage.FULLNAME_INVALID);
        }
    }
}
