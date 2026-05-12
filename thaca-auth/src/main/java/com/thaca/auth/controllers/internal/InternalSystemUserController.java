package com.thaca.auth.controllers.internal;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.common.dtos.internal.SystemUserDTO;
import com.thaca.common.dtos.internal.UserLockHistoryDTO;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalSystemUserController {

    private final FwApiProcess process;

    @PostMapping("/cms/system-users/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_SYSTEM_USERS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<SystemUserDTO>> searchSystemUsers(SearchRequest<SystemUserDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/system-users/search-lock-histories")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_USER_LOCK_HISTORY, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<UserLockHistoryDTO>> searchUserLockHistories(
        SearchRequest<UserLockHistoryDTO> request
    ) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/system-users/get")
    @FwRequest(name = ServiceMethod.CMS_GET_SYSTEM_USER, type = RequestType.INTERNAL)
    public ResponseEntity<SystemUserDTO> getSystemUser(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/system-users/create")
    @FwRequest(name = ServiceMethod.CMS_CREATE_SYSTEM_USER, type = RequestType.INTERNAL)
    public ResponseEntity<SystemUserDTO> createSystemUser(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/system-users/update")
    @FwRequest(name = ServiceMethod.CMS_UPDATE_SYSTEM_USER, type = RequestType.INTERNAL)
    public ResponseEntity<SystemUserDTO> updateSystemUser(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/system-users/lock-unlock")
    @FwRequest(name = ServiceMethod.CMS_LOCK_UNLOCK_SYSTEM_USER, type = RequestType.INTERNAL, isSuperAdmin = true)
    public ResponseEntity<Void> lockUnlockSystemUser(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/system-users/export")
    @FwRequest(name = ServiceMethod.CMS_EXPORT_SYSTEM_USER, type = RequestType.INTERNAL, isSuperAdmin = true)
    public void exportSystemUsers(SearchRequest<SystemUserDTO> request, HttpServletResponse response)
        throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-system-user-export-{{date}}.xlsx");
    }
}
