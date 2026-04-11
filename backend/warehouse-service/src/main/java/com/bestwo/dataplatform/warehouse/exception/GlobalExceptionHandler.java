package com.bestwo.dataplatform.warehouse.exception;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        log.warn("business exception", ex);
        return ApiResponse.fail(resolveMessage(ex, "business request failed"));
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException ex) {
        log.warn("bind exception", ex);
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError != null) {
            return ApiResponse.fail(fieldError.getDefaultMessage());
        }
        if (!ex.getBindingResult().getAllErrors().isEmpty()) {
            return ApiResponse.fail(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        }
        return ApiResponse.fail("invalid request parameters");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("unexpected exception", ex);
        return ApiResponse.fail(resolveMessage(ex, "warehouse request failed"));
    }

    private String resolveMessage(Throwable throwable, String fallbackMessage) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                return message;
            }
            current = current.getCause();
        }
        return fallbackMessage;
    }
}
