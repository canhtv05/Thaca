package com.thaca.framework.blocking.starter.services;

import com.thaca.common.dtos.search.PaginationRequest;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public String getCurrentUserLogin() {
        return SecurityUtils.getCurrentUsername();
    }

    public <T> void validateSearchRequest(SearchRequest<T> request) {
        if (request == null) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        PaginationRequest page = request.getPage();
        if (page == null) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (page.getPage() == null) {
            page.setPage(DEFAULT_PAGE);
        }
        if (page.getSize() == null) {
            page.setSize(DEFAULT_SIZE);
        }
        if (page.getPage() < 0) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (page.getSize() <= 0 || page.getSize() > MAX_PAGE_SIZE) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }
}
