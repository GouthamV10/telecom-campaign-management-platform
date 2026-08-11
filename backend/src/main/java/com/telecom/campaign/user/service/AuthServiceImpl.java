package com.telecom.campaign.user.service;

import com.telecom.campaign.auth.service.JwtService;
import com.telecom.campaign.user.dto.LoginRequest;
import com.telecom.campaign.user.dto.LoginResponse;
import com.telecom.campaign.user.entity.User;
import com.telecom.campaign.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
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
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        boolean isPasswordTrue = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());

        if (!isPasswordTrue) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return LoginResponse.builder().token(token).build();

    }
}
