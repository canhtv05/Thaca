package com.thaca.auth.controllers.external;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.common.dtos.internal.PermissionDTO;
import com.thaca.common.dtos.internal.RoleDTO;
import com.thaca.common.dtos.internal.req.RoleCodesReq;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import com.thaca.framework.core.utils.CommonUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminRolePermissionController {

    private final FwApiProcess process;

    @PostMapping("/roles/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_ROLES, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<RoleDTO>> searchRoles(SearchRequest<RoleDTO> criteria) {
        return ResponseEntity.ok(process.process(criteria));
    }

    @PostMapping("/roles/all")
    @FwRequest(name = ServiceMethod.CMS_GET_ALL_ROLES, type = RequestType.PROTECTED)
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/permissions/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_PERMISSIONS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<PermissionDTO>> searchPermissions(SearchRequest<PermissionDTO> criteria) {
        return ResponseEntity.ok(process.process(criteria));
    }

    @PostMapping("/permissions/all")
    @FwRequest(name = ServiceMethod.CMS_GET_ALL_PERMISSIONS, type = RequestType.PROTECTED)
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/permissions/by-roles")
    @FwRequest(name = ServiceMethod.CMS_GET_PERMISSIONS_BY_ROLES, type = RequestType.PROTECTED)
    public ResponseEntity<List<PermissionDTO>> getPermissionsByRoles(RoleCodesReq request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/roles/export")
    @FwRequest(name = ServiceMethod.CMS_EXPORT_ROLES, type = RequestType.PROTECTED, isSuperAdmin = true)
    public void exportRoles(SearchRequest<RoleDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-roles-export-{{date}}.xlsx");
    }

    @PostMapping("/permissions/export")
    @FwRequest(name = ServiceMethod.CMS_EXPORT_PERMISSIONS, type = RequestType.PROTECTED, isSuperAdmin = true)
    public void exportPermissions(SearchRequest<PermissionDTO> request, HttpServletResponse response)
        throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-permissions-export-{{date}}.xlsx");
    }
}
