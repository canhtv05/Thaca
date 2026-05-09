package com.thaca.auth.controllers.internal;

import com.thaca.common.constants.InternalMethod;
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
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final FwApiProcess process;

    @PostMapping("/cms/users/search")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_SEARCH_USERS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<UserDTO>> searchUsers(SearchRequest<UserDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/users/download-template")
    @FwRequest(
        name = InternalMethod.INTERNAL_CMS_DOWNLOAD_USER_TEMPLATE,
        type = RequestType.INTERNAL,
        isSuperAdmin = true
    )
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(null), "thaca-users-template-{{date}}.xlsx");
    }

    @PostMapping(value = "/cms/users/import", consumes = "multipart/form-data")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_IMPORT_USERS, type = RequestType.INTERNAL, isSuperAdmin = true)
    public ResponseEntity<ImportResponseDTO> importUsers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(process.process(file));
    }

    @PostMapping("/cms/users/export-error")
    @FwRequest(
        name = InternalMethod.INTERNAL_CMS_EXPORT_USER_IMPORT_ERROR,
        type = RequestType.INTERNAL,
        isSuperAdmin = true
    )
    public void exportUserImportError(ImportResponseDTO importResult, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(
            response,
            process.process(importResult),
            "thaca-users-export-error-{{date}}.xlsx"
        );
    }
}
