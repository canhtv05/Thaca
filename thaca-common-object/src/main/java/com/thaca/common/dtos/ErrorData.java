package com.thaca.common.dtos;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thaca.common.validations.ErrorMessageRule;
import java.util.HashMap;
import java.util.Map;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorData implements ErrorMessageRule {

    private String code;
    private String titleVi;
    private String titleEn;
    private String messageVi;
    private String messageEn;

    @JsonIgnore
    private Map<String, Object> data;

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }

    @JsonAnySetter
    public void setData(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
    }

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
