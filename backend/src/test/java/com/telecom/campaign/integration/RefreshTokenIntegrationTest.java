package com.telecom.campaign.integration;

import tools.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RefreshTokenIntegrationTest {

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
    void setup() {
        campaignRepository.deleteAll();
        userRepository.deleteAll();
        TestDataUtil.createUser(userRepository, passwordEncoder, "admin", "admin@example.com", "password123", "ADMIN");
    }

    @Test
    void loginReturnsAccessTokenAndRefreshToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", "admin@example.com", "password", "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertThat(data.path("token").asText()).isNotBlank();
        assertThat(data.path("refreshToken").asText()).isNotBlank();
        assertThat(data.path("expiresIn").asLong()).isPositive();
    }

    @Test
    void refreshTokenReturnsNewAccessToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", "admin@example.com", "password", "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginData = mapper.readTree(loginResult.getResponse().getContentAsString()).path("data");
        String refreshToken = loginData.path("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode refreshData = mapper.readTree(refreshResult.getResponse().getContentAsString()).path("data");
        assertThat(refreshData.path("token").asText()).isNotBlank();
        assertThat(refreshData.path("refreshToken").asText()).isNotBlank();
    }

    @Test
    void refreshTokenWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refreshToken", "invalid-token"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenWithMissingTokenReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest());
    }
}
