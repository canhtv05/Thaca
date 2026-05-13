package com.thaca.auth.controllers.internal;

import com.thaca.auth.constants.ServiceMethod;
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
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final FwApiProcess process;

    @PostMapping("/cms/users/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_USERS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<UserDTO>> searchUsers(SearchRequest<UserDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/users/detail")
    @FwRequest(name = ServiceMethod.CMS_DETAIL_USER, type = RequestType.INTERNAL)
    public ResponseEntity<UserDTO> detailUser(UserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/users/download-template")
    @FwRequest(name = ServiceMethod.CMS_DOWNLOAD_USER_TEMPLATE, type = RequestType.INTERNAL)
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(null), "thaca-users-template-{{date}}.xlsx");
    }

    @PostMapping("/cms/users/export")
    @FwRequest(name = ServiceMethod.CMS_EXPORT_USERS, type = RequestType.INTERNAL)
    public void exportUsers(SearchRequest<UserDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-users-{{date}}.xlsx");
    }

    @PostMapping(value = "/cms/users/import", consumes = "multipart/form-data")
    @FwRequest(name = ServiceMethod.CMS_IMPORT_USERS, type = RequestType.INTERNAL)
    public ResponseEntity<ImportResponseDTO> importUsers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(process.process(file));
    }

    @PostMapping("/cms/users/file-error")
    @FwRequest(name = ServiceMethod.CMS_EXPORT_USER_FILE_ERROR, type = RequestType.INTERNAL)
    public void exportUserFileError(ImportResponseDTO importResult, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(importResult), "thaca-users-file-error-{{date}}.xlsx");
    }

    // sau này checkpermission chỉ cho phép lock tenant thuộc tenant quản lý
    @PostMapping("/cms/users/lock-unlock")
    @FwRequest(name = ServiceMethod.CMS_LOCK_UNLOCK_USER, type = RequestType.INTERNAL)
    public ResponseEntity<Void> lockUnlockUser(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }
}
