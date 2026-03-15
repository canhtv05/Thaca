package com.thaca.common.dtos;

import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.validations.ErrorMessageRule;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private ApiHeader header;
    private ApiBody<T> body;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .header(ApiHeader.builder().build())
                .body(ApiBody.success(data))
                .build();
    }

    public static <T> ApiResponse<T> success(T data, PaginationResponse pagination) {
        return ApiResponse.<T>builder()
                .header(ApiHeader.builder().build())
                .body(ApiBody.success(data, pagination))
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorMessageRule error) {
        return ApiResponse.<T>builder()
                .header(ApiHeader.builder().build())
                .body(ApiBody.error(error))
                .build();
    }
}
