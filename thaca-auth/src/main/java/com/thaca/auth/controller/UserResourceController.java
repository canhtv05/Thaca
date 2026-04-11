package com.thaca.auth.controller;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.dtos.req.UserSearchReq;
import com.thaca.auth.services.UserService;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequestMode;
import com.thaca.framework.core.enums.RequestType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserResourceController {

    private final UserService userService;

    @PostMapping("/users/r/search-datatable")
    @FwRequestMode(name = ServiceMethod.ADMIN_SEARCH_USERS, type = RequestType.PROTECTED)
    public SearchResponse<UserDTO> search(SearchRequest<UserSearchReq> criteria) {
        return userService.searchDatatable(criteria);
    }

    @GetMapping("/users/r/{id}")
    @FwRequestMode(name = ServiceMethod.ADMIN_GET_USER_BY_ID, type = RequestType.PROTECTED)
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping("/users/c/create")
    @FwRequestMode(name = ServiceMethod.ADMIN_CREATE_USER, type = RequestType.PROTECTED)
    public void createUser(UserDTO userDTO) {
        userService.createUser(userDTO, true);
    }

    @PutMapping("/users/u/update")
    @FwRequestMode(name = ServiceMethod.ADMIN_UPDATE_USER, type = RequestType.PROTECTED)
    public UserDTO updateUser(UserDTO userDTO) {
        return userService.updateUser(userDTO);
    }

    @GetMapping("/users/u/lock/{id}")
    @FwRequestMode(name = ServiceMethod.ADMIN_LOCK_USER, type = RequestType.PROTECTED)
    public void lockUser(@PathVariable Long id) {
        userService.changeLockUser(id, true);
    }

    @GetMapping("/users/u/unlock/{id}")
    @FwRequestMode(name = ServiceMethod.ADMIN_UNLOCK_USER, type = RequestType.PROTECTED)
    public void unlockUser(@PathVariable Long id) {
        userService.changeLockUser(id, false);
    }
}
