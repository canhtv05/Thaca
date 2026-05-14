package com.thaca.cms.domains.projection;

import java.time.LocalDate;

public interface TenantInfoProjection {
    Long getId();

    String getName();

    String getCode();

    String getDomain();

    String getStatus();

    Long getPlanId();

    LocalDate getExpiresAt();

    String getContactEmail();

    String getLogoUrl();

    Long getVersion();

    String getPlanName();

    String getPlanCode();

    String getPlanType();
}
