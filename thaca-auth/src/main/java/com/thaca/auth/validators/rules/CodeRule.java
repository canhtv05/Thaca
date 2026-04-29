package com.thaca.auth.validators.rules;

import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.validators.core.ValidateRule;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import org.apache.commons.lang3.StringUtils;

public class CodeRule implements ValidateRule<String> {

    @Override
    public void validate(String input) {
        if (StringUtils.isBlank(input)) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (input.trim().length() > 255) {
            throw new FwException(ErrorMessage.CODE_INVALID);
        }
        String REGEX_CODE = "^[a-zA-Z0-9_-]+$";
        if (!input.trim().matches(REGEX_CODE)) {
            throw new FwException(ErrorMessage.CODE_INVALID);
        }
    }
}
