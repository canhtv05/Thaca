package com.thaca.framework.blocking.starter.services;

import com.thaca.common.dtos.ErrorData;
import com.thaca.framework.core.dtos.ApiBody;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.framework.core.utils.JsonF;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalApiServiceClient {

    private final RestTemplate restTemplate;

    /**
     * Call API nội bộ chuẩn ApiPayload (Chỉ hỗ trợ POST)
     *
     * @param url          Đường dẫn API
     * @param requestData  Dữ liệu nghiệp vụ (sẽ được bọc vào ApiPayload.body.data)
     * @param responseType Kiểu dữ liệu trả về mong muốn
     * @param <T>          Kiểu của Request Data
     * @param <R>          Kiểu của Response
     * @return Dữ liệu trả về từ API
     */
    public <T, R> R post(String url, T requestData, ParameterizedTypeReference<R> responseType) {
        log.info("[InternalApiClient] POST Request to URL: {}", url);

        // 1. Build ApiHeader chuẩn Internal
        ApiHeader header = ApiHeader.builder()
            .channel(ChannelType.INTERNAL.name())
            .username(SecurityUtils.getCurrentUsername())
            .timestamp(System.currentTimeMillis())
            .build();

        // 2. Build ApiBody kèm transId từ MDC
        ApiBody<T> body = ApiBody.<T>builder().transId(MDC.get("transId")).status("OK").data(requestData).build();

        // 3. Đóng gói vào ApiPayload
        ApiPayload<T> payload = ApiPayload.<T>builder().header(header).body(body).build();

        try {
            // 4. Thực hiện call
            ResponseEntity<R> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(payload),
                responseType
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            String responseBodyAsString = ex.getResponseBodyAsString(StandardCharsets.UTF_8);
            if (!responseBodyAsString.isEmpty()) {
                try {
                    ApiPayload<ErrorData> errorPayload = JsonF.jsonToObject(
                        responseBodyAsString,
                        new TypeReference<>() {}
                    );
                    if (
                        errorPayload != null &&
                        errorPayload.getBody() != null &&
                        errorPayload.getBody().getData() != null
                    ) {
                        ErrorData errorData = errorPayload.getBody().getData();
                        if (errorData.code() != null) {
                            throw new FwException(errorData);
                        }
                    }
                } catch (FwException fwEx) {
                    throw fwEx;
                } catch (Exception parseEx) {
                    log.error("[InternalApiClient] Error parsing error response: {}", parseEx.getMessage());
                }
            }
            throw ex;
        }
    }
}
