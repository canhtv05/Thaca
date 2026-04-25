package com.thaca.auth.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thaca.auth.domains.LoginHistory;
import com.thaca.auth.enums.DeviceType;
import com.thaca.auth.enums.LoginStatus;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.utils.DateUtils;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginHistoryDTO {

    private Long id;
    private Long userId;
    private String ipAddress;
    private String country;
    private String city;
    private Double latitude;
    private Double longitude;
    private String approxLocation;
    private DeviceType deviceType;
    private String os;
    private String browser;
    private String userAgent;
    private ChannelType channel;
    private String loginTime;
    private String requestId;
    private String deviceId;
    private Boolean isTrustedDevice;
    private Boolean isNewDevice;
    private Boolean isVpn;
    private Integer riskScore;
    private String countryIsoCode;
    private LoginStatus status;
    private String failureReason;

    private Boolean isCms;
    private Instant fromDate;
    private Instant toDate;

    public static LoginHistoryDTO fromEntity(LoginHistory entity) {
        if (entity == null) return null;
        return LoginHistoryDTO.builder()
            .id(entity.getId())
            .userId(entity.getUser() != null ? entity.getUser().getId() : null)
            .ipAddress(entity.getIpAddress())
            .country(entity.getCountry())
            .city(entity.getCity())
            .latitude(entity.getLatitude())
            .longitude(entity.getLongitude())
            .approxLocation(entity.getApproxLocation())
            .deviceType(entity.getDeviceType())
            .os(entity.getOs())
            .browser(entity.getBrowser())
            .userAgent(entity.getUserAgent())
            .channel(entity.getChannel())
            .loginTime(DateUtils.dateToString(entity.getLoginTime()))
            .requestId(entity.getRequestId())
            .deviceId(entity.getDeviceId())
            .isTrustedDevice(entity.getIsTrustedDevice())
            .isNewDevice(entity.getIsNewDevice())
            .isVpn(entity.getIsVpn())
            .riskScore(entity.getRiskScore())
            .countryIsoCode(entity.getCountryIsoCode())
            .status(entity.getStatus())
            .failureReason(entity.getFailureReason())
            .build();
    }
}
