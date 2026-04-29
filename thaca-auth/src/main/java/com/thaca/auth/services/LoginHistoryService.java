package com.thaca.auth.services;

import com.thaca.auth.domains.LoginHistory;
import com.thaca.auth.domains.SystemUser;
import com.thaca.auth.dtos.DeviceInfoDTO;
import com.thaca.auth.dtos.GeoInfoDTO;
import com.thaca.auth.enums.LoginStatus;
import com.thaca.auth.repositories.LoginHistoryRepository;
import com.thaca.auth.repositories.SystemUserRepository;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.common.dtos.internal.SystemUserDTO;
import com.thaca.framework.core.context.FwContextBody;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.enums.ChannelType;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;
    private final SystemUserRepository systemUserRepository;
    private final ClientContextService commonService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLoginHistory(
        SystemUserDTO userDTO,
        HttpServletRequest request,
        LoginStatus status,
        String failureReason,
        boolean isCms
    ) {
        String ip = commonService.extractIpAddress(request);
        String ua = request.getHeader("User-Agent");
        String deviceId = Optional.ofNullable(FwContextHeader.get())
            .map(h -> StringUtils.trimToNull(h.getDeviceId()))
            .orElse(null);

        GeoInfoDTO geo = commonService.lookup(ip);
        DeviceInfoDTO device = commonService.parse(ua);
        boolean isNewDevice = isNewDevice(userDTO.getId(), isCms, deviceId, status);

        LoginHistory history = LoginHistory.builder()
            .user(isCms ? null : userRepository.getReferenceById(userDTO.getId()))
            .systemUser(isCms ? SystemUser.builder().id(userDTO.getId()).build() : null)
            .ipAddress(ip)
            .userAgent(ua)
            .browser(device.getBrowser())
            .os(device.getOs())
            .deviceType(device.getDeviceType())
            .country(geo.getCountry())
            .countryIsoCode(geo.getCountryIsoCode())
            .city(geo.getCity())
            .latitude(geo.getLatitude())
            .longitude(geo.getLongitude())
            .approxLocation(geo.getApproxLocation())
            .isVpn(geo.getIsVpn())
            .riskScore(geo.getRiskScore())
            .channel(
                ChannelType.valueOf(
                    StringUtils.defaultIfBlank(FwContextHeader.get().getChannel(), ChannelType.WEB.name())
                )
            )
            .status(status)
            .failureReason(failureReason)
            .loginTime(Instant.now())
            .requestId(FwContextBody.get().getTransId())
            .deviceId(deviceId)
            .isNewDevice(isNewDevice)
            .tenantId(userDTO.getTenantId())
            .build();

        loginHistoryRepository.save(history);
    }

    private boolean isNewDevice(Long userId, boolean isCms, String deviceId, LoginStatus status) {
        if (userId == null || StringUtils.isBlank(deviceId) || !LoginStatus.SUCCESS.equals(status)) {
            return false;
        }
        if (isCms) {
            return !loginHistoryRepository.existsBySystemUser_IdAndDeviceIdAndStatus(
                userId,
                deviceId,
                LoginStatus.SUCCESS
            );
        }
        return !loginHistoryRepository.existsByUser_IdAndDeviceIdAndStatus(userId, deviceId, LoginStatus.SUCCESS);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedLoginAttempt(Long systemUserId) {
        SystemUser su = systemUserRepository.findById(systemUserId).orElseThrow();
        int attempts = (su.getFailedLoginAttempts() == null ? 0 : su.getFailedLoginAttempts()) + 1;
        su.setFailedLoginAttempts(attempts);
        if (attempts >= 5) {
            su.setIsLocked(true);
            su.setLockedUntil(Instant.now().plusSeconds(30 * 60));
        }
        systemUserRepository.save(su);
        return attempts;
    }
}
