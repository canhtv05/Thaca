package com.thaca.cms.services;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.ImportResponseDTO;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.blocking.starter.services.InternalApiClient;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsUserImportService {

    private final InternalApiClient internalApiClient;
    private final FrameworkProperties frameworkProperties;

    @FwMode(name = ServiceMethod.CMS_IMPORT_USERS, type = ModeType.HANDLE)
    public ImportResponseDTO importUsers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        try {
            MultiValueMap<String, Object> parts = getStringObjectMultiValueMap(file);
            String url = frameworkProperties.getRoutes().getAuthService() + "/internal/cms/users/import";
            return internalApiClient.postMultipart(url, parts, new ParameterizedTypeReference<>() {});
        } catch (IOException e) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    private static MultiValueMap<String, Object> getStringObjectMultiValueMap(MultipartFile file) throws IOException {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        InputStreamResource resource = new InputStreamResource(file.getInputStream()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.xlsx";
            }

            @Override
            public long contentLength() {
                return file.getSize();
            }
        };
        parts.add("file", resource);
        return parts;
    }
}
