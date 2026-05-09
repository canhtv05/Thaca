package com.thaca.framework.core.filter;

import com.thaca.framework.core.dtos.ApiBody;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.utils.JsonF;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class MultipartRequestWrapper extends HttpServletRequestWrapper {

    private final ApiPayload<?> envelope;

    public MultipartRequestWrapper(HttpServletRequest request) {
        super(request);
        this.envelope = parseEnvelope(request);
    }

    public ApiPayload<?> extractEnvelope() {
        return envelope;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ApiPayload<?> parseEnvelope(HttpServletRequest request) {
        String contentType = request.getContentType();
        boolean isMultipart =
            contentType != null && contentType.toLowerCase().contains(MediaType.MULTIPART_FORM_DATA_VALUE);
        if (!isMultipart) {
            return null;
        }

        String headerStr = request.getParameter("header");
        String bodyStr = request.getParameter("body");

        if (headerStr != null && !headerStr.isEmpty()) {
            ApiHeader header = JsonF.jsonToObject(headerStr, ApiHeader.class);
            ApiBody body =
                bodyStr != null && !bodyStr.isEmpty()
                    ? JsonF.jsonToObject(bodyStr, ApiBody.class)
                    : ApiBody.builder().build();
            return ApiPayload.builder().header(header).body(body).build();
        }
        return null;
    }
}
