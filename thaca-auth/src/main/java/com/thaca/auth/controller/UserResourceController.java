package com.thaca.auth.controller;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.dtos.UserPermissionDTO;
import com.thaca.auth.dtos.req.UserSearchReq;
import com.thaca.auth.enums.PermissionAction;
import com.thaca.auth.services.UserService;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequestMode;
import com.thaca.framework.core.dtos.ApiEnvelope;
import com.thaca.framework.core.enums.RequestType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserResourceController {

    private final UserService userService;

    @PostMapping("/users/r/search-datatable")
    @FwRequestMode(name = ServiceMethod.ADMIN_SEARCH_USERS, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<SearchResponse<UserDTO>>> search(
        @ModelAttribute SearchRequest<UserSearchReq> criteria
    ) {
        SearchResponse<UserDTO> result = userService.searchDatatable(criteria);
        return ResponseEntity.ok(ApiEnvelope.success(result));
    }

    @GetMapping("/users/r/{id}")
    @FwRequestMode(name = ServiceMethod.ADMIN_GET_USER_BY_ID, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO result = userService.findById(id);
        return ResponseEntity.ok(ApiEnvelope.success(result));
    }

    @PostMapping("/users/c/create")
    @FwRequestMode(name = ServiceMethod.ADMIN_CREATE_USER, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        UserDTO newUserDTO = userService.createUser(userDTO, true);
        return ResponseEntity.ok(ApiEnvelope.success(newUserDTO));
    }

    @PutMapping("/users/u/update")
    @FwRequestMode(name = ServiceMethod.ADMIN_UPDATE_USER, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<UserDTO>> updateUser(@RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(userDTO);
        return ResponseEntity.ok(ApiEnvelope.success(updatedUser));
    }

    @GetMapping("/users/u/lock/{id}")
    @FwRequestMode(name = ServiceMethod.ADMIN_LOCK_USER, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<Boolean>> lockUser(@PathVariable Long id) {
        userService.changeLockUser(id, true);
        return ResponseEntity.ok(ApiEnvelope.success());
    }

    @GetMapping("/users/u/unlock/{id}")
    @FwRequestMode(name = ServiceMethod.ADMIN_UNLOCK_USER, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<Boolean>> unlockUser(@PathVariable Long id) {
        userService.changeLockUser(id, false);
        return ResponseEntity.ok(ApiEnvelope.success());
    }

    @GetMapping("/user-permission/r/{id}")
    @FwRequestMode(name = ServiceMethod.ADMIN_GET_USER_PERMISSION, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<Map<String, PermissionAction>>> getUserPermission(@PathVariable Long id) {
        Map<String, PermissionAction> result = userService.getUserPermissions(id);
        return ResponseEntity.ok(ApiEnvelope.success(result));
    }

    @PostMapping("/user-permission/u/{id}")
    @FwRequestMode(name = ServiceMethod.ADMIN_UPDATE_USER_PERMISSION, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<Boolean>> updateUserPermission(
        @PathVariable Long id,
        @RequestBody List<UserPermissionDTO> request
    ) {
        userService.updateUserPermission(id, request);
        return ResponseEntity.ok(ApiEnvelope.success());
    }
}
