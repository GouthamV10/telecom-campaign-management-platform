package com.telecom.campaign.user.service;

import com.telecom.campaign.common.enums.Role;
import com.telecom.campaign.exception.EmailAlreadyExistsException;
import com.telecom.campaign.exception.InvalidUserRoleException;
import com.telecom.campaign.exception.ResourceNotFoundException;
import com.telecom.campaign.exception.WeakPasswordException;
import com.telecom.campaign.user.dto.ChangePasswordRequest;
import com.telecom.campaign.user.dto.CreateUserRequest;
import com.telecom.campaign.user.dto.RegisterRequest;
import com.telecom.campaign.user.dto.UserResponse;
import com.telecom.campaign.user.entity.User;
import com.telecom.campaign.user.mapper.UserMapper;
import com.telecom.campaign.user.repository.UserRepository;
import com.telecom.campaign.user.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public UserResponse register(RegisterRequest request) {

        log.info("Registering user email={}", request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest){
        log.info("createUser called for email={}", createUserRequest.getEmail());
        if(userRepository.existsByEmail(createUserRequest.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists");
        }

        if (createUserRequest.getRole() == Role.ADMIN) {
            throw new InvalidUserRoleException(
                    "Cannot create another ADMIN user"
            );
        }

        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        user.setEmail(createUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setRole(createUserRequest.getRole());
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }
    @Override
    public List<UserResponse> getAllUsers() {

        log.info("Fetching all users");

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public Page<UserResponse> getUsers(String keyword, Role role, Boolean enabled, Pageable pageable) {
        log.info("Fetching users with filters keyword={} role={} enabled={}", keyword, role, enabled);

        Specification<User> specification = null;

        if (keyword != null && !keyword.isBlank()) {
            specification = UserSpecification.hasKeyword(keyword);
        }

        if (role != null) {
            specification = specification == null
                    ? UserSpecification.hasRole(role)
                    : specification.and(UserSpecification.hasRole(role));
        }

        if (enabled != null) {
            specification = specification == null
                    ? UserSpecification.isEnabled(enabled)
                    : specification.and(UserSpecification.isEnabled(enabled));
        }

        Page<User> users = userRepository.findAll(specification, pageable);
        return users.map(userMapper::toResponse);
    }

    @Override
    public UserResponse toggleUserEnabled(Long id, boolean enabled) {
        log.info("Toggling user id={} enabled={}", id, enabled);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request, Long authenticatedUserId) {
        log.info("Changing password for userId={} requestedBy={}", userId, authenticatedUserId);

        if (!userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("You can only change your own password");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AccessDeniedException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new WeakPasswordException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        log.info("Fetching current user id={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

}
