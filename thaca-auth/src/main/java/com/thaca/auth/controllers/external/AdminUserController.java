package com.thaca.auth.controllers.external;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.SystemUserDTO;
import com.thaca.common.dtos.internal.ImportResponseDTO;
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
@RequestMapping("/auth/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final FwApiProcess process;

    @PostMapping("/search")
    @FwRequest(name = ServiceMethod.ADMIN_SEARCH_USERS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<UserDTO>> searchUsers(SearchRequest<UserDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/detail")
    @FwRequest(name = ServiceMethod.ADMIN_DETAIL_USER, type = RequestType.PROTECTED)
    public ResponseEntity<UserDTO> detailUser(UserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/download-template")
    @FwRequest(name = ServiceMethod.ADMIN_DOWNLOAD_USER_TEMPLATE, type = RequestType.PROTECTED)
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(null), "thaca-users-template-{{date}}.xlsx");
    }

    @PostMapping("/export")
    @FwRequest(name = ServiceMethod.ADMIN_EXPORT_USERS, type = RequestType.PROTECTED)
    public void exportUsers(SearchRequest<UserDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-users-{{date}}.xlsx");
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @FwRequest(name = ServiceMethod.ADMIN_IMPORT_USERS, type = RequestType.PROTECTED)
    public ResponseEntity<ImportResponseDTO> importUsers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(process.process(file));
    }

    @PostMapping("/file-error")
    @FwRequest(name = ServiceMethod.ADMIN_EXPORT_USER_FILE_ERROR, type = RequestType.PROTECTED)
    public void exportUserFileError(ImportResponseDTO importResult, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(importResult), "thaca-users-file-error-{{date}}.xlsx");
    }

    @PostMapping("/lock-unlock")
    @FwRequest(name = ServiceMethod.ADMIN_LOCK_UNLOCK_USER, type = RequestType.PROTECTED)
    public ResponseEntity<Void> lockUnlockUser(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }
}
