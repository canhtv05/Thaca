package com.thaca.auth.services;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.*;
import com.thaca.auth.mappers.UserLockHistoryMapper;
import com.thaca.auth.repositories.UserLockHistoryRepository;
import com.thaca.common.dtos.internal.UserLockHistoryDTO;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.blocking.starter.services.CommonService;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLockHistoryService {

    private final UserLockHistoryRepository userLockHistoryRepository;

    @FwMode(name = ServiceMethod.CMS_SEARCH_USER_LOCK_HISTORY, type = ModeType.VALIDATE)
    public void validateSearchUserLockHistory(SearchRequest<Void> request) {
        CommonService.validateSearchRequest(request);
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.CMS_SEARCH_USER_LOCK_HISTORY, type = ModeType.HANDLE)
    public SearchResponse<UserLockHistoryDTO> searchUserLockHistory(SearchRequest<UserLockHistoryDTO> request) {
        Specification<UserLockHistory> spec = createSpecification(request);
        if (request.getFilter().getTargetUserId() != null) {
            Page<UserLockHistory> reasons = userLockHistoryRepository.findAll(
                spec,
                request.getPage().toPageable(Sort.Direction.DESC, "createdAt")
            );
            return new SearchResponse<>(
                reasons.getContent().stream().map(UserLockHistoryMapper::fromEntity).collect(Collectors.toList()),
                PaginationResponse.of(reasons)
            );
        } else {
            return new SearchResponse<>(Collections.emptyList(), PaginationResponse.empty());
        }
    }

    private Specification<UserLockHistory> createSpecification(SearchRequest<UserLockHistoryDTO> request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getFilter() == null || request.getFilter().getTargetUserId() == null) return null;
            UserLockHistoryDTO f = request.getFilter();
            predicates.add(cb.equal(root.get("targetUserId"), f.getTargetUserId()));
            if (f.getAction() != null) predicates.add(
                cb.like(cb.lower(root.get("action")), "%" + f.getAction().name().toLowerCase() + "%")
            );
            if (StringUtils.isNotBlank(f.getReason())) predicates.add(
                cb.like(cb.lower(root.get("reason")), "%" + f.getReason().toLowerCase() + "%")
            );
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
