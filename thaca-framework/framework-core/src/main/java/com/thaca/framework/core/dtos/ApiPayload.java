package com.thaca.framework.core.dtos;

import com.thaca.common.dtos.ErrorData;
import com.thaca.common.validations.ErrorMessageRule;
import com.thaca.framework.core.context.FwContextHeader;
import java.util.Map;
import lombok.*;
import org.slf4j.MDC;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiPayload<T> {

    private ApiHeader header;
    private ApiBody<T> body;

    public static <T> ApiPayload<T> success(T data) {
        return ApiPayload.<T>builder().header(fallbackHeader()).body(ApiBody.success(MDC.get("transId"), data)).build();
    }

    public static <T> ApiPayload<T> success() {
        return ApiPayload.<T>builder().header(fallbackHeader()).body(ApiBody.success(MDC.get("transId"))).build();
    }

    public static ApiPayload<ErrorData> error(ErrorMessageRule error) {
        return error(error, null, null, null);
    }

    public static ApiPayload<ErrorData> error(ErrorMessageRule error, Map<String, Object> data) {
        return error(error, null, null, data);
    }

    public static ApiPayload<ErrorData> error(
        ErrorMessageRule error,
        String customMessageVi,
        String customMessageEn,
        Map<String, Object> data
    ) {
        return ApiPayload.<ErrorData>builder()
            .header(fallbackHeader())
            .body(ApiBody.error(MDC.get("transId"), error, customMessageVi, customMessageEn, data))
            .build();
    }

    public static ApiHeader fallbackHeader() {
        if (FwContextHeader.get() == null) {
            return ApiHeader.builder().build();
        }
        return FwContextHeader.get();
    }
}
