package com.thaca.auth.controller;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.services.UserService;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms")
@RequiredArgsConstructor
public class UserResourceController {

    private final UserService userService;

    @PostMapping("/users/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_USERS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<UserDTO>> search(SearchRequest<UserDTO> criteria) {
        return ResponseEntity.ok(userService.search(criteria));
    }

    @GetMapping("/users/r/{id}")
    @FwRequest(name = ServiceMethod.CMS_GET_USER_BY_ID, type = RequestType.PROTECTED)
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping("/users/c/create")
    @FwRequest(name = ServiceMethod.CMS_CREATE_USER, type = RequestType.PROTECTED)
    public void createUser(@RequestBody UserDTO userDTO) {
        userService.createUser(userDTO, true);
    }

    @PutMapping("/users/u/update")
    @FwRequest(name = ServiceMethod.CMS_UPDATE_USER, type = RequestType.PROTECTED)
    public UserDTO updateUser(@RequestBody UserDTO userDTO) {
        return userService.updateUser(userDTO);
    }

    @GetMapping("/users/u/lock/{id}")
    @FwRequest(name = ServiceMethod.CMS_LOCK_USER, type = RequestType.PROTECTED)
    public void lockUser(@PathVariable Long id) {
        userService.changeLockUser(id, true);
    }

    @GetMapping("/users/u/unlock/{id}")
    @FwRequest(name = ServiceMethod.CMS_UNLOCK_USER, type = RequestType.PROTECTED)
    public void unlockUser(@PathVariable Long id) {
        userService.changeLockUser(id, false);
    }
}
