package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.AuthUserDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.blocking.starter.services.InternalApiServiceClient;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cms")
@RequiredArgsConstructor
public class UserResourceController {

    private final InternalApiServiceClient internalApiClient;
    private static final String AUTH_SERVICE_URL = "http://localhost:1001/internal";

    @PostMapping("/sign-in")
    @FwRequest(name = ServiceMethod.CMS_AUTHENTICATE, type = RequestType.PUBLIC)
    public ResponseEntity<AuthenticateRes> signIn(LoginReq loginReq) {
        return ResponseEntity.ok(
            internalApiClient.post(AUTH_SERVICE_URL + "/cms/sign-in", loginReq, new ParameterizedTypeReference<>() {})
        );
    }

    @PostMapping("/profile")
    @FwRequest(name = ServiceMethod.CMS_GET_PROFILE, type = RequestType.PROTECTED)
    public ResponseEntity<AuthUserDTO> getProfile() {
        return ResponseEntity.ok(
            internalApiClient.post(AUTH_SERVICE_URL + "/cms/profile", null, new ParameterizedTypeReference<>() {})
        );
    }

    @PostMapping("/users/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_USERS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<UserDTO>> search(SearchRequest<UserDTO> criteria) {
        return ResponseEntity.ok(
            internalApiClient.post(
                AUTH_SERVICE_URL + "/cms/users/search",
                criteria,
                new ParameterizedTypeReference<>() {}
            )
        );
    }

    @PostMapping("/users/lock")
    @FwRequest(name = ServiceMethod.CMS_LOCK_USER, type = RequestType.PROTECTED)
    public ResponseEntity<Void> lockUser(Long id) {
        internalApiClient.post(AUTH_SERVICE_URL + "/cms/users/lock", id, new ParameterizedTypeReference<Void>() {});
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/unlock")
    @FwRequest(name = ServiceMethod.CMS_UNLOCK_USER, type = RequestType.PROTECTED)
    public ResponseEntity<Void> unlockUser(Long id) {
        internalApiClient.post(AUTH_SERVICE_URL + "/cms/users/unlock", id, new ParameterizedTypeReference<Void>() {});
        return ResponseEntity.ok().build();
    }
}
