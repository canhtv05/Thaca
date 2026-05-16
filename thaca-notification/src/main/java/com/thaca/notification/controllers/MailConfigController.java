package com.thaca.notification.controllers;

import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import com.thaca.notification.constants.ServiceMethod;
import com.thaca.notification.dtos.MailConfigDTO;
import com.thaca.notification.dtos.req.TestConnectionReq;
import com.thaca.notification.dtos.res.TestConnectionRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification/mail-config")
@RequiredArgsConstructor
public class MailConfigController {

    private final FwApiProcess process;

    @PostMapping("/search")
    @FwRequest(name = ServiceMethod.MAIL_CONFIG_SEARCH, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<MailConfigDTO>> search(SearchRequest<MailConfigDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/create")
    @FwRequest(name = ServiceMethod.MAIL_CONFIG_CREATE, type = RequestType.PROTECTED)
    public ResponseEntity<Void> create(MailConfigDTO request) {
        process.process(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    @FwRequest(name = ServiceMethod.MAIL_CONFIG_UPDATE, type = RequestType.PROTECTED)
    public ResponseEntity<Void> update(MailConfigDTO request) {
        process.process(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    @FwRequest(name = ServiceMethod.MAIL_CONFIG_TEST_CONNECTION, type = RequestType.PROTECTED)
    public ResponseEntity<TestConnectionRes> testConnection(TestConnectionReq request) {
        return ResponseEntity.ok(process.process(request));
    }
}
