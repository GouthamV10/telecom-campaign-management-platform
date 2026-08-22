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

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DashboardIntegrationTest {

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
    }

    @Test
    void dashboardStatsReturnsCorrectCounts() throws Exception {
        TestDataUtil.createUser(userRepository, passwordEncoder, "admin", "admin@example.com", "password123", "ADMIN");
        TestDataUtil.createUser(userRepository, passwordEncoder, "mgr", "mgr@example.com", "password123", "MANAGER");
        TestDataUtil.createUser(userRepository, passwordEncoder, "user1", "user1@example.com", "password123", "USER");

        String adminToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "admin@example.com", "password123");

        // Create campaigns
        Map<String, Object> p1 = Map.of("name", "Camp1", "description", "d", "startDate", LocalDateTime.now().plusDays(1).toString(), "endDate", LocalDateTime.now().plusDays(5).toString());
        Map<String, Object> p2 = Map.of("name", "Camp2", "description", "d", "startDate", LocalDateTime.now().plusDays(1).toString(), "endDate", LocalDateTime.now().plusDays(5).toString());
        Map<String, Object> p3 = Map.of("name", "Camp3", "description", "d", "startDate", LocalDateTime.now().plusDays(1).toString(), "endDate", LocalDateTime.now().plusDays(5).toString());

        var r1 = mockMvc.perform(post("/api/campaigns").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(p1)))
                .andExpect(status().isCreated()).andReturn();
        var r2 = mockMvc.perform(post("/api/campaigns").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(p2)))
                .andExpect(status().isCreated()).andReturn();
        var r3 = mockMvc.perform(post("/api/campaigns").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(p3)))
                .andExpect(status().isCreated()).andReturn();

        Long id1 = mapper.readTree(r1.getResponse().getContentAsString()).path("data").path("id").asLong();
        Long id2 = mapper.readTree(r2.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Transition statuses
        mockMvc.perform(patch("/api/campaigns/" + id1 + "/status").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status", "ACTIVE"))))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/campaigns/" + id2 + "/status").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status", "CANCELLED"))))
                .andExpect(status().isOk());

        // Fetch dashboard stats
        MvcResult result = mockMvc.perform(get("/api/dashboard/stats").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andReturn();

        JsonNode stats = mapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertThat(stats.path("totalCampaigns").asInt()).isEqualTo(3);
        assertThat(stats.path("draftCampaigns").asInt()).isEqualTo(1);
        assertThat(stats.path("activeCampaigns").asInt()).isEqualTo(1);
        assertThat(stats.path("cancelledCampaigns").asInt()).isEqualTo(1);
        assertThat(stats.path("totalUsers").asInt()).isEqualTo(3);
        assertThat(stats.path("activeUsers").asInt()).isEqualTo(3);
    }

    @Test
    void dashboardStats_unauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }
}
