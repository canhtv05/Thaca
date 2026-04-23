package com.thaca.auth.dtos;

import com.thaca.auth.enums.DeviceType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {

    private String browser;
    private String os;
    private String device;
    private DeviceType deviceType;

    public static DeviceInfo unknown() {
        return DeviceInfo.builder()
            .browser("unknown")
            .device("unknown")
            .deviceType(DeviceType.UNKNOWN)
            .os("unknown")
            .build();
    }
}
