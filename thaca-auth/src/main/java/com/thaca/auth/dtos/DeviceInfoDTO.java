package com.thaca.auth.dtos;

import com.thaca.auth.enums.DeviceType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoDTO {

    private String browser;
    private String os;
    private String device;
    private DeviceType deviceType;

    public static DeviceInfoDTO unknown() {
        return DeviceInfoDTO.builder()
            .browser("unknown")
            .device("unknown")
            .deviceType(DeviceType.UNKNOWN)
            .os("unknown")
            .build();
    }
}
