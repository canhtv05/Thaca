package com.thaca.notification.dtos.res;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestConnectionRes {

    private Boolean success;
    private String titleVi;
    private String messageVi;
    private String titleEn;
    private String messageEn;
}
