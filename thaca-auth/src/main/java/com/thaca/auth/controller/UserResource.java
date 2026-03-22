package com.thaca.auth.controller;

import com.thaca.auth.dtos.ImportUserDTO;
import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.dtos.UserPermissionDTO;
import com.thaca.auth.dtos.excel.ImportExcelResult;
import com.thaca.auth.dtos.search.SearchRequest;
import com.thaca.auth.dtos.search.SearchResponse;
import com.thaca.auth.enums.PermissionAction;
import com.thaca.auth.services.UserService;
import com.thaca.common.dtos.ApiResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;

    @PostMapping("/users/r/search-datatable")
    public ResponseEntity<ApiResponse<SearchResponse<UserDTO>>> search(@ModelAttribute SearchRequest criteria) {
        SearchResponse<UserDTO> result = userService.searchDatatable(criteria);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/users/r/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO result = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/users/c/create")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        UserDTO newUserDTO = userService.createUser(userDTO, true);
        return ResponseEntity.ok(ApiResponse.success(newUserDTO));
    }

    @PutMapping("/users/u/update")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(userDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }

    @GetMapping("/users/u/lock/{id}")
    public ResponseEntity<ApiResponse<Boolean>> lockUser(@PathVariable Long id) {
        userService.changeLockUser(id, true);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/users/u/unlock/{id}")
    public ResponseEntity<ApiResponse<Boolean>> unlockUser(@PathVariable Long id) {
        userService.changeLockUser(id, false);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/users/r/export")
    public ResponseEntity<byte[]> exportUser(@RequestBody SearchRequest request) {
        String filename = "export-user-" + LocalDate.now() + ".xlsx";
        byte[] file = userService.exportUser(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(file);
    }

    @GetMapping("/users/c/file-template")
    public ResponseEntity<byte[]> downloadFileTemplate() {
        String filename = "user-import-template.xlsx";
        byte[] file = userService.downloadTemplate();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(file);
    }

    @PostMapping("/users/c/import")
    public ResponseEntity<ApiResponse<ImportExcelResult<ImportUserDTO>>> importUser(
        @RequestPart("file") MultipartFile file
    ) {
        ImportExcelResult<ImportUserDTO> result = userService.importUser(file);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/user-permission/r/{id}")
    public ResponseEntity<ApiResponse<Map<String, PermissionAction>>> getUserPermission(@PathVariable Long id) {
        Map<String, PermissionAction> result = userService.getUserPermissions(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/user-permission/u/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateUserPermission(
        @PathVariable Long id,
        @RequestBody List<UserPermissionDTO> request
    ) {
        userService.updateUserPermission(id, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
