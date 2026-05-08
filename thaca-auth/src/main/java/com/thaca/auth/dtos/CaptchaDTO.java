package com.thaca.auth.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class CaptchaDTO {

    private String captchaId;
    private String image;

    @JsonIgnore
    private String answer;

    public static enum Mode {
        TEXT,
        MATH,
        AUTO
    }
}
