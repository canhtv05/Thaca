package com.thaca.framework.core.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thaca.common.dtos.ErrorData;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.validations.ErrorMessageRule;
import com.thaca.framework.core.utils.CommonUtils;
import java.util.Map;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiBody<T> {

    private String transId;
    private String status; // OK | FAILED
    private T data;

    private PaginationResponse<?> pagination;

    public static <T> ApiBody<T> success(String transId, T data) {
        return ApiBody.<T>builder().transId(transId).status("OK").data(data).build();
    }

    public static <T> ApiBody<T> success(String transId, T data, PaginationResponse<?> pagination) {
        return ApiBody.<T>builder().transId(transId).status("OK").data(data).pagination(pagination).build();
    }

    public static <T> ApiBody<T> success(String transId) {
        return ApiBody.<T>builder().transId(transId).status("OK").build();
    }

    public static ApiBody<ErrorData> error(String transId, ErrorMessageRule error) {
        return error(transId, error, null, null, null);
    }

    public static ApiBody<ErrorData> error(String transId, ErrorMessageRule error, Map<String, Object> data) {
        return error(transId, error, null, null, data);
    }

    public static ApiBody<ErrorData> error(
        String transId,
        ErrorMessageRule error,
        String customMessageVi,
        String customMessageEn,
        Map<String, Object> data
    ) {
        String msgVi = customMessageVi != null ? customMessageVi : error.messageVi();
        String msgEn = customMessageEn != null ? customMessageEn : error.messageEn();

        msgVi = CommonUtils.formatMessage(msgVi, data);
        msgEn = CommonUtils.formatMessage(msgEn, data);

        return ApiBody.<ErrorData>builder()
            .transId(transId)
            .status("FAILED")
            .data(
                ErrorData.builder()
                    .code(error.code())
                    .titleVi(error.titleVi())
                    .titleEn(error.titleEn())
                    .messageVi(msgVi)
                    .messageEn(msgEn)
                    .data(data)
                    .build()
            )
            .build();
    }
}
