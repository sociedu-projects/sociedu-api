package com.unishare.api.config;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.common.dto.CommonErrorCode;
import com.unishare.api.common.dto.ExceptionCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException e) {
        ExceptionCode code = e.getExceptionCode();
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = code != null ? code.getType() : CommonErrorCode.INTERNAL_ERROR.getType();
        }
        return handleException(code, message);
    }

    private ResponseEntity<ApiResponse<?>> handleException(ExceptionCode e, String message) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("type", e != null ? e.getType() : CommonErrorCode.INTERNAL_ERROR.getType());
        HttpStatus status = e != null ? e.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        ApiResponse<?> errorResponse = ApiResponse.<Void>build()
                .withHttpStatus(status)
                .withCode(e != null ? e.getCode() : HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withErrors(errors)
                .withMessage(message != null ? message : "");
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("type", CommonErrorCode.VALIDATION_ERROR.getType());
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid");
        }
        errors.put("fields", fields);
        ApiResponse<?> body = ApiResponse.<Void>build()
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withCode(CommonErrorCode.VALIDATION_ERROR.getCode())
                .withErrors(errors)
                .withMessage("Dữ liệu không hợp lệ");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraint(ConstraintViolationException e) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("type", CommonErrorCode.VALIDATION_ERROR.getType());
        String details = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        errors.put("details", details);
        ApiResponse<?> body = ApiResponse.<Void>build()
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withCode(CommonErrorCode.VALIDATION_ERROR.getCode())
                .withErrors(errors)
                .withMessage("Dữ liệu không hợp lệ");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleNotReadable(HttpMessageNotReadableException e) {
        log.debug("Malformed request body: {}", e.getMessage());
        Map<String, Object> errors = new HashMap<>();
        errors.put("type", CommonErrorCode.BAD_REQUEST.getType());
        ApiResponse<?> body = ApiResponse.<Void>build()
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withCode(CommonErrorCode.BAD_REQUEST.getCode())
                .withErrors(errors)
                .withMessage("Không đọc được nội dung request (JSON không hợp lệ hoặc thiếu kiểu).");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleResponseStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        Map<String, Object> errors = new HashMap<>();
        errors.put("type", status.name());
        String reason = e.getReason() != null ? e.getReason() : status.getReasonPhrase();
        ApiResponse<?> body = ApiResponse.<Void>build()
                .withHttpStatus(status)
                .withCode(status.value())
                .withErrors(errors)
                .withMessage(reason);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException e) {
        log.debug("Illegal argument: {}", e.getMessage());
        Map<String, Object> errors = new HashMap<>();
        errors.put("type", CommonErrorCode.BAD_REQUEST.getType());
        ApiResponse<?> body = ApiResponse.<Void>build()
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withCode(CommonErrorCode.BAD_REQUEST.getCode())
                .withErrors(errors)
                .withMessage(e.getMessage() != null ? e.getMessage() : "Yêu cầu không hợp lệ");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxUpload(MaxUploadSizeExceededException e) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("type", CommonErrorCode.PAYLOAD_TOO_LARGE.getType());
        ApiResponse<?> body = ApiResponse.<Void>build()
                .withHttpStatus(HttpStatus.PAYLOAD_TOO_LARGE)
                .withCode(CommonErrorCode.PAYLOAD_TOO_LARGE.getCode())
                .withErrors(errors)
                .withMessage("Kích thước file hoặc request vượt giới hạn cho phép.");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception e) {
        log.error("Unhandled exception", e);
        Map<String, Object> errors = new HashMap<>();
        errors.put("type", CommonErrorCode.INTERNAL_ERROR.getType());
        ApiResponse<?> body = ApiResponse.<Void>build()
                .withHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .withCode(CommonErrorCode.INTERNAL_ERROR.getCode())
                .withErrors(errors)
                .withMessage("Đã xảy ra lỗi hệ thống.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
