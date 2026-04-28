package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
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
@RequestMapping("/cms")
@RequiredArgsConstructor
public class UserResourceController {

    private final FwApiProcess fwApiProcess;

    @PostMapping("/users/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_USERS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<UserDTO>> search(SearchRequest<UserDTO> criteria) {
        return ResponseEntity.ok(fwApiProcess.process(criteria));
    }

    @PostMapping("/users/lock")
    @FwRequest(name = ServiceMethod.CMS_LOCK_USER, type = RequestType.PROTECTED)
    public ResponseEntity<Void> lockUser(Long id) {
        return ResponseEntity.ok(fwApiProcess.process(id));
    }

    @PostMapping("/users/unlock")
    @FwRequest(name = ServiceMethod.CMS_UNLOCK_USER, type = RequestType.PROTECTED)
    public ResponseEntity<Void> unlockUser(Long id) {
        return ResponseEntity.ok(fwApiProcess.process(id));
    }
}
