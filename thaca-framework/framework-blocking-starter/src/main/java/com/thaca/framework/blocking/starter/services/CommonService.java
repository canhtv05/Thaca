package com.thaca.framework.blocking.starter.services;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonService {

    public String getCurrentUserLogin() {
        return SecurityUtils.getCurrentUsername();
    }
}
