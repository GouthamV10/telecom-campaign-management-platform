package com.telecom.campaign.unit;

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
import com.telecom.campaign.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void register_createsUserWithUserRole() {
        RegisterRequest request = new RegisterRequest("newuser", "new@example.com", "password123");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return UserResponse.builder().id(u.getId()).username(u.getUsername()).email(u.getEmail()).role(u.getRole()).enabled(u.getEnabled()).build();
        });

        UserResponse response = userService.register(request);

        assertThat(response.getRole()).isEqualTo(Role.USER);
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("newuser", "existing@example.com", "password123");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void createUser_createsManagerSuccessfully() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("manager1");
        request.setEmail("mgr@example.com");
        request.setPassword("pass1234");
        request.setRole(Role.MANAGER);

        when(userRepository.existsByEmail("mgr@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return UserResponse.builder().id(u.getId()).role(u.getRole()).build();
        });

        UserResponse response = userService.createUser(request);

        assertThat(response.getRole()).isEqualTo(Role.MANAGER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_throwsWhenCreatingAdmin() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin2");
        request.setEmail("admin2@example.com");
        request.setPassword("pass1234");
        request.setRole(Role.ADMIN);

        when(userRepository.existsByEmail("admin2@example.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(InvalidUserRoleException.class);
    }

    @Test
    void toggleUserEnabled_disablesUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return UserResponse.builder().id(u.getId()).enabled(u.getEnabled()).build();
        });

        UserResponse response = userService.toggleUserEnabled(1L, false);

        assertThat(response.getEnabled()).isFalse();
        verify(userRepository).save(testUser);
    }

    @Test
    void toggleUserEnabled_throwsWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.toggleUserEnabled(999L, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changePassword_success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldpass");
        request.setNewPassword("newpass123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpass", "encoded-password")).thenReturn(true);
        when(passwordEncoder.matches("newpass123", "encoded-password")).thenReturn(false);
        when(passwordEncoder.encode("newpass123")).thenReturn("new-encoded");

        userService.changePassword(1L, request, 1L);

        verify(userRepository).save(testUser);
        assertThat(testUser.getPassword()).isEqualTo("new-encoded");
    }

    @Test
    void changePassword_throwsWhenChangingOtherUserPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldpass");
        request.setNewPassword("newpass123");

        assertThatThrownBy(() -> userService.changePassword(1L, request, 2L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void changePassword_throwsWhenCurrentPasswordWrong() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongpass");
        request.setNewPassword("newpass123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, request, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void changePassword_throwsWhenNewPasswordSameAsCurrent() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("samepass");
        request.setNewPassword("samepass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("samepass", "encoded-password")).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(1L, request, 1L))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void getCurrentUser_returnsUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(
                UserResponse.builder().id(1L).username("testuser").email("test@example.com").role(Role.USER).enabled(true).build()
        );

        UserResponse response = userService.getCurrentUser(1L);

        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getCurrentUser_throwsWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getUsers_withFilters_returnsPage() {
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findAll((Specification<User>) any(), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(testUser)).thenReturn(
                UserResponse.builder().id(1L).username("testuser").role(Role.USER).enabled(true).build()
        );

        Page<UserResponse> result = userService.getUsers("test", Role.USER, true, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUsers_noFilters_returnsAllUsers() {
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findAll((Specification<User>) any(), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(testUser)).thenReturn(
                UserResponse.builder().id(1L).username("testuser").role(Role.USER).enabled(true).build()
        );

        Page<UserResponse> result = userService.getUsers(null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}
