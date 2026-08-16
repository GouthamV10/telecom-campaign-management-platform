package com.telecom.campaign.exception;

import com.telecom.campaign.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex) {

        log.warn("AccessDeniedException: {}", ex.getMessage());

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(false)
                        .statusCode(403)
                        .message(ex.getMessage())
                        .data(null)
                        .build();

        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){

        log.info("EmailAlreadyExistsException: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).statusCode(409).message(ex.getMessage()).data(null).build();
        return ResponseEntity.status(409).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> handleValidationException(MethodArgumentNotValidException ex){

        log.info("Validation failed: {} errors", ex.getFieldErrors().size());

        Map<String, List<String>> errors = new HashMap<>();

        for (FieldError error : ex.getFieldErrors()) {
            errors.computeIfAbsent(error.getField(), key -> new ArrayList<>())
                    .add(error.getDefaultMessage());
        }

        ApiResponse<Map<String, List<String>>> response = ApiResponse.<Map<String, List<String>>>builder().success(false).statusCode(400).message("Validation Errors").data(errors).build();
        return  ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex){
        log.info("ResourceNotFoundException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).statusCode(404).message(ex.getMessage()).data(null).build();
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(InvalidCampaignStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCampaignStatusException(InvalidCampaignStatusException ex){
        log.info("InvalidCampaignStatusException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).statusCode(400).message(ex.getMessage()).data(null).build();
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(InvalidUserRoleException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidUserRoleException(InvalidUserRoleException ex){
        log.info("InvalidUserRoleException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).statusCode(400).message(ex.getMessage()).data(null).build();
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.info("BadCredentialsException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).statusCode(401).message(ex.getMessage()).data(null).build();
        return ResponseEntity.status(401).body(response);
    }
}
