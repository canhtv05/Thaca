package com.thaca.auth.controllers.external;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.SystemUserDTO;
import com.thaca.auth.dtos.UserLockHistoryDTO;
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
@RequestMapping("/auth/admin/system-users")
@RequiredArgsConstructor
public class AdminSystemUserController {

    private final FwApiProcess process;

    @PostMapping("/search")
    @FwRequest(name = ServiceMethod.ADMIN_SEARCH_SYSTEM_USERS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<SystemUserDTO>> searchSystemUsers(SearchRequest<SystemUserDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/search-lock-histories")
    @FwRequest(name = ServiceMethod.ADMIN_SEARCH_USER_LOCK_HISTORY, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<UserLockHistoryDTO>> searchUserLockHistories(
        SearchRequest<UserLockHistoryDTO> request
    ) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/get")
    @FwRequest(name = ServiceMethod.ADMIN_GET_SYSTEM_USER, type = RequestType.PROTECTED)
    public ResponseEntity<SystemUserDTO> getSystemUser(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/create")
    @FwRequest(name = ServiceMethod.ADMIN_CREATE_SYSTEM_USER, type = RequestType.PROTECTED)
    public ResponseEntity<SystemUserDTO> createSystemUser(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/update")
    @FwRequest(name = ServiceMethod.ADMIN_UPDATE_SYSTEM_USER, type = RequestType.PROTECTED)
    public ResponseEntity<SystemUserDTO> updateSystemUser(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/lock-unlock")
    @FwRequest(name = ServiceMethod.ADMIN_LOCK_UNLOCK_SYSTEM_USER, type = RequestType.PROTECTED, isSuperAdmin = true)
    public ResponseEntity<Void> lockUnlockSystemUser(SystemUserDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/export")
    @FwRequest(name = ServiceMethod.ADMIN_EXPORT_SYSTEM_USER, type = RequestType.PROTECTED, isSuperAdmin = true)
    public void exportSystemUsers(SearchRequest<SystemUserDTO> request, HttpServletResponse response)
        throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-system-user-export-{{date}}.xlsx");
    }
}
