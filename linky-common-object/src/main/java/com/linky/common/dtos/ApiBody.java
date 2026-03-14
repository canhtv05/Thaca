package com.linky.common.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.linky.common.dtos.search.PaginationResponse;
import com.linky.common.validations.ErrorMessageRule;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiBody<T> {

    private String code;
    private String messageVi;
    private String messageEn;
    private Map<String, Object> errors;
    private T data;
    private PaginationResponse pagination;

    public static <T> ApiBody<T> success() {
        return ApiBody.<T>builder()
                .code("200")
                .messageVi("Thành công")
                .messageEn("Success")
                .build();
    }

    public static <T> ApiBody<T> success(T data) {
        return ApiBody.<T>builder()
                .code("200")
                .messageVi("Thành công")
                .messageEn("Success")
                .data(data)
                .build();
    }

    public static <T> ApiBody<T> success(T data, PaginationResponse pagination) {
        return ApiBody.<T>builder()
                .code("200")
                .messageVi("Thành công")
                .messageEn("Success")
                .data(data)
                .pagination(pagination)
                .build();
    }

    public static <T> ApiBody<T> error(String code, String messageVi, String messageEn) {
        return ApiBody.<T>builder()
                .code(code)
                .messageVi(messageVi)
                .messageEn(messageEn)
                .build();
    }

    public static <T> ApiBody<T> error(ErrorMessageRule errorMessage) {
        return ApiBody.<T>builder()
                .code(errorMessage.code())
                .messageVi(errorMessage.messageVi())
                .messageEn(errorMessage.messageEn())
                .build();
    }

    public static <T> ApiBody<T> error(ErrorMessageRule errorMessage, T data) {
        return ApiBody.<T>builder()
                .code(errorMessage.code())
                .messageVi(errorMessage.messageVi())
                .messageVi(errorMessage.messageEn())
                .data(data)
                .build();
    }
}
