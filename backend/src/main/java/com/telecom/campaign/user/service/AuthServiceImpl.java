package com.telecom.campaign.user.service;

import com.telecom.campaign.user.service.JwtService;
import com.telecom.campaign.user.dto.LoginRequest;
import com.telecom.campaign.user.dto.LoginResponse;
import com.telecom.campaign.user.entity.User;
import com.telecom.campaign.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for email={}", loginRequest.getEmail());
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.getEnabled()) {
            throw new BadCredentialsException("Account is disabled. Contact your administrator.");
        }

        boolean isPasswordTrue = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());

        if (!isPasswordTrue) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return LoginResponse.builder().token(token).refreshToken(refreshToken).expiresIn(jwtService.getExpiration()).build();

    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Processing token refresh");

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String email = jwtService.extractUsernameFromRefresh(refreshToken);
        if (email == null) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.getEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }

        String newToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        return LoginResponse.builder().token(newToken).refreshToken(newRefreshToken).expiresIn(jwtService.getExpiration()).build();
    }
}
