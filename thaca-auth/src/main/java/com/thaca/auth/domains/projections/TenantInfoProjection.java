package com.thaca.auth.domains.projections;

public interface TenantInfoProjection {
    Long getId();

    String getName();

    String getCode();

    String getLogoUrl();
}
