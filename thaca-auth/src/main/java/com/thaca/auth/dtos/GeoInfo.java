package com.thaca.auth.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoInfo {

    private String country;
    private String countryIsoCode;
    private String city;
    private Double latitude;
    private Double longitude;
    private String approxLocation;
    private Boolean isVpn;
    private Boolean isProxy;
    private Boolean isHosting;
    private Boolean isTor;
    private Integer riskScore;

    public static GeoInfo unknown() {
        return GeoInfo.builder()
            .country("unknown")
            .countryIsoCode("unknown")
            .city("unknown")
            .latitude(null)
            .longitude(null)
            .approxLocation("unknown")
            .isVpn(false)
            .isProxy(false)
            .isHosting(false)
            .isTor(false)
            .riskScore(0)
            .build();
    }
}
