package com.telecom.campaign.user.controller;

import com.telecom.campaign.common.dto.ApiResponse;
import com.telecom.campaign.user.dto.CreateUserRequest;
import com.telecom.campaign.user.dto.RegisterRequest;
import com.telecom.campaign.user.dto.UserResponse;
import com.telecom.campaign.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest registerRequest){
        log.info("register called for email={}", registerRequest.getEmail());
        UserResponse userResponse = userService.register(registerRequest);
        ApiResponse<UserResponse> registrationResponse = ApiResponse.<UserResponse>builder().success(true).statusCode(201).message("Successfully Registered").data(userResponse).build();
        return ResponseEntity.status(201).body(registrationResponse);
    }

    @GetMapping("/admin-test")
    public ResponseEntity<ApiResponse<String>> adminTest() {

        log.info("adminTest endpoint called");

        ApiResponse<String> response =
                ApiResponse.<String>builder()
                        .success(true)
                        .statusCode(200)
                        .message("Admin access granted")
                        .data("You are an ADMIN")
                        .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest createUserRequest){
        log.info("createUser called for email={} role={}", createUserRequest.getEmail(), createUserRequest.getRole());
        UserResponse userResponse = userService.createUser(createUserRequest);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder().success(true).statusCode(201).message("User Created Successfully").data(userResponse).build();
        return ResponseEntity.status(201).body(response);
    }
}
