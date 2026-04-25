package com.thaca.auth.domains;

import com.thaca.auth.enums.DeviceType;
import com.thaca.auth.enums.LoginStatus;
import com.thaca.framework.core.enums.ChannelType;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "login_history", schema = "auth")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_login_seq")
    @SequenceGenerator(name = "user_login_seq", allocationSize = 1)
    private Long id;

    @Column(name = "username", length = 50)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_user_id", referencedColumnName = "id")
    private SystemUser systemUser;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "approx_location", length = 255)
    private String approxLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    @Column(name = "os", length = 100)
    private String os;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20)
    private ChannelType channel;

    @Builder.Default
    @Column(name = "login_time", nullable = false)
    private Instant loginTime = Instant.now();

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Builder.Default
    @Column(name = "is_trusted_device")
    private Boolean isTrustedDevice = false;

    @Builder.Default
    @Column(name = "is_new_device")
    private Boolean isNewDevice = false;

    @Builder.Default
    @Column(name = "is_vpn")
    private Boolean isVpn = false;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "country_iso_code", length = 20)
    private String countryIsoCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private LoginStatus status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;
}
