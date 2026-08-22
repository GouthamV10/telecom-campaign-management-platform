package com.telecom.campaign.user.controller;

import com.telecom.campaign.common.dto.ApiResponse;
import com.telecom.campaign.user.dto.LoginRequest;
import com.telecom.campaign.user.dto.LoginResponse;
import com.telecom.campaign.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest){
        log.info("login attempt for email={}", loginRequest.getEmail());
        LoginResponse loginResponse = authService.login(loginRequest);
        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder().success(true).statusCode(200).message("Login successful").data(loginResponse).build();
        return ResponseEntity.status(200).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestBody Map<String, String> body){
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            ApiResponse<LoginResponse> error = ApiResponse.<LoginResponse>builder().success(false).statusCode(400).message("Refresh token is required").data(null).build();
            return ResponseEntity.badRequest().body(error);
        }
        log.info("token refresh requested");
        LoginResponse loginResponse = authService.refreshToken(refreshToken);
        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder().success(true).statusCode(200).message("Token refreshed successfully").data(loginResponse).build();
        return ResponseEntity.ok(response);
    }
}
