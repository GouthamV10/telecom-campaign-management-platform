package com.telecom.campaign.exception;

import com.telecom.campaign.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){

        ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).statusCode(409).message(ex.getMessage()).data(null).build();
        return ResponseEntity.status(409).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> handleValidationException(MethodArgumentNotValidException ex){

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
        ApiResponse<Void> response = ApiResponse.<Void>builder().success(false).statusCode(404).message(ex.getMessage()).data(null).build();
        return ResponseEntity.status(404).body(response);
    }
}
