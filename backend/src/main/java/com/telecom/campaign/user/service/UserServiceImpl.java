package com.telecom.campaign.user.service;

import com.telecom.campaign.common.enums.Role;
import com.telecom.campaign.exception.EmailAlreadyExistsException;
import com.telecom.campaign.exception.InvalidUserRoleException;
import com.telecom.campaign.user.dto.CreateUserRequest;
import com.telecom.campaign.user.dto.RegisterRequest;
import com.telecom.campaign.user.dto.UserResponse;
import com.telecom.campaign.user.entity.User;
import com.telecom.campaign.user.mapper.UserMapper;
import com.telecom.campaign.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
}
