package com.telecom.campaign.user.controller;

import com.telecom.campaign.common.dto.ApiResponse;
import com.telecom.campaign.common.enums.Role;
import com.telecom.campaign.user.dto.ChangePasswordRequest;
import com.telecom.campaign.user.dto.CreateUserRequest;
import com.telecom.campaign.user.dto.RegisterRequest;
import com.telecom.campaign.user.dto.UserResponse;
import com.telecom.campaign.user.entity.User;
import com.telecom.campaign.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled,
            Pageable pageable) {

        log.info("getUsers called keyword={} role={} enabled={}", keyword, role, enabled);

        Page<UserResponse> users = userService.getUsers(keyword, role, enabled, pageable);

        ApiResponse<Page<UserResponse>> response =
                ApiResponse.<Page<UserResponse>>builder()
                        .success(true)
                        .statusCode(200)
                        .message("Users Fetched Successfully")
                        .data(users)
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.info("getCurrentUser called");
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserResponse userResponse = userService.getCurrentUser(authenticatedUser.getId());
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .statusCode(200)
                .message("User Fetched Successfully")
                .data(userResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        log.info("toggleUserEnabled called for id={} enabled={}", id, enabled);
        UserResponse userResponse = userService.toggleUserEnabled(id, enabled);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .statusCode(200)
                .message(enabled ? "User Enabled Successfully" : "User Disabled Successfully")
                .data(userResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("changePassword called for userId={}", id);
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changePassword(id, request, authenticatedUser.getId());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .statusCode(200)
                .message("Password Changed Successfully")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }
}
