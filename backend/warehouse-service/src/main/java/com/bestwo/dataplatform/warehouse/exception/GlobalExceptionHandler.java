package com.bestwo.dataplatform.warehouse.exception;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.common.exception.BusinessException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException ex) {
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
        return ApiResponse.fail(ex.getMessage());
    }
}
