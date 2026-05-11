package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.ImportResponseDTO;
import com.thaca.common.dtos.internal.SystemUserDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import com.thaca.framework.core.utils.CommonUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/cms")
@RequiredArgsConstructor
public class UserController {

    private final FwApiProcess process;

    @PostMapping("/users/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_USERS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<UserDTO>> search(SearchRequest<UserDTO> criteria) {
        return ResponseEntity.ok(process.process(criteria));
    }

    @PostMapping("/users/detail")
    @FwRequest(name = ServiceMethod.CMS_DETAIL_USER, type = RequestType.PUBLIC)
    public ResponseEntity<UserDTO> detailUser(UserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/users/download-template")
    @FwRequest(name = ServiceMethod.CMS_DOWNLOAD_USER_TEMPLATE, type = RequestType.PROTECTED)
    public void downloadUserTemplate(HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(null), "thaca-users-template-{{date}}.xlsx");
    }

    @PostMapping(value = "/users/import", consumes = "multipart/form-data")
    @FwRequest(name = ServiceMethod.CMS_IMPORT_USERS, type = RequestType.PROTECTED)
    public ResponseEntity<ImportResponseDTO> importUsers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(process.process(file));
    }

    @PostMapping("/users/file-error")
    @FwRequest(name = ServiceMethod.CMS_EXPORT_USER_FILE_ERROR, type = RequestType.PROTECTED)
    public void exportUserFileError(ImportResponseDTO importResult, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(importResult), "thaca-users-file-error-{{date}}.xlsx");
    }

    // sau này checkpermission chỉ cho phép lock tenant thuộc tenant quản lý
    @PostMapping("/users/lock-unlock")
    @FwRequest(name = ServiceMethod.CMS_LOCK_UNLOCK_USER, type = RequestType.PROTECTED)
    public ResponseEntity<Void> lockUnlock(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }
}
