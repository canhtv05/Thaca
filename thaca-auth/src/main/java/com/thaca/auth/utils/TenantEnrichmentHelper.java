package com.thaca.auth.utils;

import com.thaca.auth.clients.CmsClient;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.contracts.TenantAwareDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.common.enums.TenantStatus;
import com.thaca.framework.core.security.SecurityUtils;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@RequiredArgsConstructor
public class TenantEnrichmentHelper {

    private final CmsClient cmsClient;

    public TenantDTO fromTenantInfoPrj(TenantInfoPrj t) {
        return TenantDTO.builder()
            .id(t.getId())
            .code(t.getCode())
            .name(t.getName())
            .domain(t.getDomain())
            .status(t.getStatus() != null ? TenantStatus.valueOf(t.getStatus()) : null)
            .planId(t.getPlanId())
            .expiresAt(t.getExpiresAt())
            .contactEmail(t.getContactEmail())
            .logoUrl(t.getLogoUrl())
            .version(t.getVersion())
            .build();
    }

    public List<Long> getVisibleTenantIds(List<Long> tenantIds) {
        if (CollectionUtils.isEmpty(tenantIds)) return Collections.emptyList();
        if (SecurityUtils.isSuperAdmin()) return tenantIds;
        Long currentTenantId = SecurityUtils.getCurrentTenantId();
        return tenantIds
            .stream()
            .filter(id -> Objects.equals(id, currentTenantId))
            .toList();
    }

    public <T extends TenantAwareDTO> T enrichTenantFull(T dto) {
        if (!CollectionUtils.isEmpty(dto.getTenantIds())) {
            List<Long> visibleTenantIds = getVisibleTenantIds(dto.getTenantIds());
            if (!visibleTenantIds.isEmpty()) {
                List<TenantInfoPrj> tenants = cmsClient.getTenantsByIds(
                    TenantDTO.builder().tenantIds(new ArrayList<>(visibleTenantIds)).build()
                );
                dto.setTenants(tenants.stream().map(this::fromTenantInfoPrj).collect(Collectors.toList()));
                dto.setTenantInfos(null);
                dto.setTenantIds(new ArrayList<>(visibleTenantIds));
            } else {
                dto.setTenants(Collections.emptyList());
            }
        }
        return dto;
    }

    public <T extends TenantAwareDTO> void enrichTenantFull(T dto, Map<Long, TenantInfoPrj> tenantMap) {
        List<Long> visibleTenantIds = getVisibleTenantIds(dto.getTenantIds());
        if (!visibleTenantIds.isEmpty()) {
            dto.setTenants(
                visibleTenantIds
                    .stream()
                    .map(tenantMap::get)
                    .filter(Objects::nonNull)
                    .map(this::fromTenantInfoPrj)
                    .collect(Collectors.toList())
            );
            dto.setTenantInfos(null);
            dto.setTenantIds(new ArrayList<>(visibleTenantIds));
        } else {
            dto.setTenants(Collections.emptyList());
        }
    }

    public <T extends TenantAwareDTO> void enrichTenantInfo(T dto, Map<Long, TenantInfoPrj> tenantMap) {
        List<Long> visibleTenantIds = getVisibleTenantIds(dto.getTenantIds());
        if (!visibleTenantIds.isEmpty()) {
            dto.setTenantInfos(
                visibleTenantIds.stream().map(tenantMap::get).filter(Objects::nonNull).collect(Collectors.toList())
            );
            dto.setTenants(null);
            dto.setTenantIds(new ArrayList<>(visibleTenantIds));
        } else {
            dto.setTenantInfos(Collections.emptyList());
        }
    }

    public Map<Long, TenantInfoPrj> fetchTenantMap(Set<Long> tenantIds) {
        if (tenantIds.isEmpty()) return Collections.emptyMap();

        Set<Long> filteredIds = tenantIds;
        if (!SecurityUtils.isSuperAdmin()) {
            List<Long> currentTenantIds = SecurityUtils.getCurrentTenantIds();
            filteredIds = tenantIds.stream().filter(currentTenantIds::contains).collect(Collectors.toSet());
        }

        if (filteredIds.isEmpty()) return Collections.emptyMap();

        List<TenantInfoPrj> tenants = cmsClient.getTenantsByIds(
            TenantDTO.builder().tenantIds(new ArrayList<>(filteredIds)).build()
        );
        return tenants.stream().collect(Collectors.toMap(TenantInfoPrj::getId, t -> t));
    }

    public <T extends TenantAwareDTO> Set<Long> collectTenantIds(List<T> dtos) {
        return dtos
            .stream()
            .filter(d -> !CollectionUtils.isEmpty(d.getTenantIds()))
            .flatMap(d -> d.getTenantIds().stream())
            .collect(Collectors.toSet());
    }
}
