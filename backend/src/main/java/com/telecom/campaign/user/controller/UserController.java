package com.telecom.campaign.user.controller;

import com.telecom.campaign.common.dto.ApiResponse;
import com.telecom.campaign.user.dto.RegisterRequest;
import com.telecom.campaign.user.dto.UserResponse;
import com.telecom.campaign.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest registerRequest){
        UserResponse userResponse = userService.register(registerRequest);
        ApiResponse<UserResponse> registrationResponse = ApiResponse.<UserResponse>builder().success(true).statusCode(201).message("Successfully Registered").data(userResponse).build();
        return ResponseEntity.status(201).body(registrationResponse);
    }

}
