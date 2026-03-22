package com.thaca.common.dtos;

import com.thaca.common.validations.ErrorMessageRule;
import java.util.UUID;
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
        String transId = UUID.randomUUID().toString();

        return ApiResponse.<T>builder()
            .header(ApiHeader.builder().build())
            .body(ApiBody.success(transId, data))
            .build();
    }

    public static <T> ApiResponse<T> success() {
        String transId = UUID.randomUUID().toString();

        return ApiResponse.<T>builder().header(ApiHeader.builder().build()).body(ApiBody.success(transId)).build();
    }

    public static ApiResponse<ErrorData> error(ErrorMessageRule error) {
        String transId = UUID.randomUUID().toString();
        return ApiResponse.<ErrorData>builder()
            .header(ApiHeader.builder().build())
            .body(ApiBody.error(transId, error))
            .build();
    }
}
