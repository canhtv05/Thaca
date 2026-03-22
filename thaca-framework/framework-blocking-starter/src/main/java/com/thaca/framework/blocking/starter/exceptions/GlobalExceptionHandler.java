package com.thaca.framework.blocking.starter.exceptions;

import com.thaca.common.dtos.ApiResponse;
import com.thaca.common.dtos.ErrorData;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.exceptions.FwException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorData>> handleException(Exception ex) {
        log.error("[GlobalExceptionHandler] exception]:: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.error(CommonErrorMessage.INTERNAL_SERVER_ERROR)
        );
    }

    @ExceptionHandler(FwException.class)
    public ResponseEntity<ApiResponse<ErrorData>> handleFwException(FwException ex) {
        log.error("[GlobalExceptionHandler] FwException]:: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getErrorMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<ErrorData>> handleNotFound(NoResourceFoundException ex) {
        log.error("[GlobalExceptionHandler] resource not found]:: ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(CommonErrorMessage.NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<ErrorData>> handleMethodAllowed(HttpRequestMethodNotSupportedException ex) {
        log.error("[GlobalExceptionHandler] method not allowed]:: ", ex);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
            ApiResponse.error(CommonErrorMessage.METHOD_NOT_ALLOWED)
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorData>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("[GlobalExceptionHandler] access denied]:: ", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(CommonErrorMessage.FORBIDDEN));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ErrorData>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("[GlobalExceptionHandler] request body invalid]:: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.error(CommonErrorMessage.INVALID_REQUEST_BODY)
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<ErrorData>> handleBindException(BindException ex) {
        log.error("[GlobalExceptionHandler] binding error]:: ", ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(
            ApiResponse.error(CommonErrorMessage.BINDING_ERROR)
        );
    }
}
