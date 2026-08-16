package com.telecom.campaign.integration;

import tools.jackson.databind.ObjectMapper;
import com.telecom.campaign.integration.util.TestDataUtil;
import com.telecom.campaign.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.telecom.campaign.campaign.repository.CampaignRepository;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup(){
        // ensure campaigns cleaned first because of FK
        campaignRepository.deleteAll();
        userRepository.deleteAll();
        TestDataUtil.createUser(userRepository, passwordEncoder, "admin", "admin@example.com", "password123", "ADMIN");
    }

    @Test
    void validAdminLoginReturnsToken() throws Exception{
        String token = TestDataUtil.loginAndGetToken(mockMvc, mapper, "admin@example.com", "password123");
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    void invalidPasswordReturns401() throws Exception{
        var payload = mapper.createObjectNode();
        payload.put("email","admin@example.com");
        payload.put("password","wrongpass");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }
}
