package com.telecom.campaign.integration.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.telecom.campaign.user.entity.User;
import com.telecom.campaign.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestDataUtil {

    public static User createUser(UserRepository userRepository, PasswordEncoder passwordEncoder, String username, String email, String rawPassword, String role){
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(com.telecom.campaign.common.enums.Role.valueOf(role));
        user.setEnabled(true);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }

    public static String loginAndGetToken(MockMvc mockMvc, ObjectMapper mapper, String email, String password) throws Exception{
        Map<String,String> payload = Map.of("email", email, "password", password);
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType("application/json").content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode root = mapper.readTree(content);
        JsonNode tokenNode = root.path("data").path("token");
        return tokenNode.isTextual() ? tokenNode.asText() : null;
    }
}
