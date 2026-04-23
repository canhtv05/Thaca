package com.thaca.auth.domains;

import com.thaca.auth.enums.DeviceType;
import com.thaca.auth.enums.LoginMethod;
import com.thaca.auth.enums.LoginStatus;
import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import com.thaca.framework.core.enums.ChannelType;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "login_history", schema = "auth")
public class LoginHistory extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "thaca_seq_gen")
    @SequenceGenerator(name = "thaca_seq_gen", sequenceName = "thaca_seq", allocationSize = 1)
    @Column(name = "id", length = 50, nullable = false, unique = true)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

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

    @Column(name = "device", length = 100)
    private String device;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "login_method", length = 20)
    private LoginMethod loginMethod;

    @Builder.Default
    @Column(name = "login_time", nullable = false)
    private Instant loginTime = Instant.now();

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "request_id", length = 100)
    private String requestId;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private LoginStatus status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;
}
