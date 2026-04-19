package com.bestwo.dataplatform.order.exception;

import com.bestwo.dataplatform.common.api.ApiResponse;
import com.bestwo.dataplatform.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        log.warn("business exception {}", StructuredArguments.keyValue("event", "business_exception"), exception);
        return ApiResponse.of(exception.getCode(), exception.getMessage(), null);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public ApiResponse<Void> handleValidationException(Exception exception) {
        log.warn("validation exception {}", StructuredArguments.keyValue("event", "validation_exception"), exception);
        return ApiResponse.fail(resolveValidationMessage(exception));
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("unexpected exception {}", StructuredArguments.keyValue("event", "unexpected_exception"), exception);
        return ApiResponse.fail(exception.getMessage() == null ? "internal server error" : exception.getMessage());
    }

    private String resolveValidationMessage(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException
            && methodArgumentNotValidException.getBindingResult().getFieldError() != null) {
            return methodArgumentNotValidException.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (exception instanceof BindException bindException && bindException.getBindingResult().getFieldError() != null) {
            return bindException.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (exception instanceof ConstraintViolationException constraintViolationException
            && !constraintViolationException.getConstraintViolations().isEmpty()) {
            return constraintViolationException.getConstraintViolations().iterator().next().getMessage();
        }
        return "invalid request";
    }
}
