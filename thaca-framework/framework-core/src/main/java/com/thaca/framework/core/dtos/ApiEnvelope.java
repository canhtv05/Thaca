package com.thaca.framework.core.dtos;

import com.thaca.common.dtos.ErrorData;
import com.thaca.common.validations.ErrorMessageRule;
import com.thaca.framework.core.context.FwContext;
import lombok.*;
import org.slf4j.MDC;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiEnvelope<T> {

    private ApiHeader header;
    private ApiBody<T> body;

    public static <T> ApiEnvelope<T> success(T data) {
        return ApiEnvelope.<T>builder()
            .header(fallbackHeader())
            .body(ApiBody.success(MDC.get("transId"), data))
            .build();
    }

    public static <T> ApiEnvelope<T> success() {
        return ApiEnvelope.<T>builder().header(fallbackHeader()).body(ApiBody.success(MDC.get("transId"))).build();
    }

    public static ApiEnvelope<ErrorData> error(ErrorMessageRule error) {
        return ApiEnvelope.<ErrorData>builder()
            .header(fallbackHeader())
            .body(ApiBody.error(MDC.get("transId"), error))
            .build();
    }

    public static ApiHeader fallbackHeader() {
        if (FwContext.get() == null) {
            return ApiHeader.builder().build();
        }
        return FwContext.get();
    }
}
