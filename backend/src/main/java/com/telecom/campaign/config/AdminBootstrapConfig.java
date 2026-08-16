package com.telecom.campaign.config;

import com.telecom.campaign.common.enums.Role;
import com.telecom.campaign.user.entity.User;
import com.telecom.campaign.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Slf4j
@Configuration
public class AdminBootstrapConfig {

    @Bean
    CommandLineRunner createInitialAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder){
        return args -> {
            String adminEmail = "admin@campaign.com";

            if(!userRepository.existsByEmail(adminEmail)){
                User user = new User();

                user.setUsername("admin");
                user.setEmail(adminEmail);
                user.setPassword(passwordEncoder.encode("Admin@123"));
                user.setEnabled(true);
                user.setRole(Role.ADMIN);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());

                userRepository.save(user);

                log.info("Initial admin user created successfully: {}", adminEmail);
            } else {
                log.info("Initial admin user already exists: {}", adminEmail);
            }
        };
    }
}
