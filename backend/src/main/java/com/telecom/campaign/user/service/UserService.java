package com.telecom.campaign.user.service;

import com.telecom.campaign.common.enums.Role;
import com.telecom.campaign.user.dto.ChangePasswordRequest;
import com.telecom.campaign.user.dto.CreateUserRequest;
import com.telecom.campaign.user.dto.RegisterRequest;
import com.telecom.campaign.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse createUser(CreateUserRequest createUserRequest);

    List<UserResponse> getAllUsers();

    Page<UserResponse> getUsers(String keyword, Role role, Boolean enabled, Pageable pageable);

    UserResponse toggleUserEnabled(Long id, boolean enabled);

    void changePassword(Long userId, ChangePasswordRequest request, Long authenticatedUserId);

    UserResponse getCurrentUser(Long userId);
}
