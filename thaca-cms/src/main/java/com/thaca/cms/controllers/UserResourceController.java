package com.thaca.cms.controllers;

import com.thaca.cms.clients.AuthClient;
import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.AuthUserDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cms")
@RequiredArgsConstructor
public class UserResourceController {

    private final AuthClient authClient;

    @PostMapping("/sign-in")
    @FwRequest(name = ServiceMethod.CMS_AUTHENTICATE, type = RequestType.PUBLIC)
    public ResponseEntity<AuthenticateRes> signIn(LoginReq loginReq) {
        return ResponseEntity.ok(authClient.signIn(loginReq));
    }

    @PostMapping("/profile")
    @FwRequest(name = ServiceMethod.CMS_GET_PROFILE, type = RequestType.PROTECTED)
    public ResponseEntity<AuthUserDTO> getProfile() {
        return ResponseEntity.ok(authClient.getProfile());
    }

    @PostMapping("/users/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_USERS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<UserDTO>> search(SearchRequest<UserDTO> criteria) {
        return ResponseEntity.ok(authClient.search(criteria));
    }

    @PostMapping("/users/lock")
    @FwRequest(name = ServiceMethod.CMS_LOCK_USER, type = RequestType.PROTECTED)
    public ResponseEntity<Void> lockUser(Long id) {
        return ResponseEntity.ok(authClient.lockUser(id));
    }

    @PostMapping("/users/unlock")
    @FwRequest(name = ServiceMethod.CMS_UNLOCK_USER, type = RequestType.PROTECTED)
    public ResponseEntity<Void> unlockUser(Long id) {
        return ResponseEntity.ok(authClient.unlockUser(id));
    }
}
