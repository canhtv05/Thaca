package com.thaca.auth.controllers.internal;

import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final FwApiProcess process;

    @PostMapping("/cms/users/search")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_SEARCH_USERS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<UserDTO>> searchUsers(SearchRequest<UserDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }
}
