package com.telecom.campaign.unit;

import com.telecom.campaign.common.enums.Role;
import com.telecom.campaign.user.dto.LoginRequest;
import com.telecom.campaign.user.dto.LoginResponse;
import com.telecom.campaign.user.entity.User;
import com.telecom.campaign.user.repository.UserRepository;
import com.telecom.campaign.user.service.AuthServiceImpl;
import com.telecom.campaign.user.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

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
    void login_validCredentialsReturnsToken() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");
        when(jwtService.getExpiration()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getExpiresIn()).isEqualTo(86400000L);
    }

    @Test
    void login_userNotFoundThrowsBadCredentials() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_disabledUserThrowsBadCredentials() {
        testUser.setEnabled(false);
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void login_wrongPasswordThrowsBadCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpass");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_validTokenReturnsNewTokens() {
        when(jwtService.isRefreshTokenValid("valid-refresh")).thenReturn(true);
        when(jwtService.extractUsernameFromRefresh("valid-refresh")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("new-jwt");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh");
        when(jwtService.getExpiration()).thenReturn(86400000L);

        LoginResponse response = authService.refreshToken("valid-refresh");

        assertThat(response.getToken()).isEqualTo("new-jwt");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refreshToken_invalidTokenThrowsBadCredentials() {
        when(jwtService.isRefreshTokenValid("invalid")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("invalid"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_disabledUserThrowsBadCredentials() {
        testUser.setEnabled(false);
        when(jwtService.isRefreshTokenValid("valid-refresh")).thenReturn(true);
        when(jwtService.extractUsernameFromRefresh("valid-refresh")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.refreshToken("valid-refresh"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("disabled");
    }
}
