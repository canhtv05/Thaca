package com.thaca.common.dtos;

import com.thaca.common.validations.ErrorMessageRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorData implements ErrorMessageRule {

    private String code;
    private String titleVi;
    private String titleEn;
    private String messageVi;
    private String messageEn;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String titleVi() {
        return titleVi;
    }

    @Override
    public String titleEn() {
        return titleEn;
    }

    @Override
    public String messageVi() {
        return messageVi;
    }

    @Override
    public String messageEn() {
        return messageEn;
    }
}
