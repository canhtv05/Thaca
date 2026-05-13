package com.thaca.framework.core.validations.rules;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.validations.ValidateRule;
import org.apache.commons.lang3.StringUtils;

public class CodeRule implements ValidateRule<String> {

    @Override
    public void validate(String input) {
        if (StringUtils.isBlank(input)) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (input.trim().length() > 255) {
            throw new FwException(CommonErrorMessage.CODE_INVALID);
        }
        String REGEX_CODE = "^[a-zA-Z0-9_-]+$";
        if (!input.trim().matches(REGEX_CODE)) {
            throw new FwException(CommonErrorMessage.CODE_INVALID);
        }
    }
}
