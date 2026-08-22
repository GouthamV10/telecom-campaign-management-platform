package com.telecom.campaign.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.telecom.campaign.common.enums.Role;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserManagementIntegrationTest {

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
    void getCurrentUser_returnsOwnInfo() throws Exception {
        TestDataUtil.createUser(userRepository, passwordEncoder, "admin", "admin@example.com", "password123", "ADMIN");
        String token = TestDataUtil.loginAndGetToken(mockMvc, mapper, "admin@example.com", "password123");

        MvcResult result = mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertThat(data.path("email").asText()).isEqualTo("admin@example.com");
        assertThat(data.path("role").asText()).isEqualTo("ADMIN");
    }

    @Test
    void toggleUserEnabled_disablesAndEnablesUser() throws Exception {
        TestDataUtil.createUser(userRepository, passwordEncoder, "admin", "admin@example.com", "password123", "ADMIN");
        TestDataUtil.createUser(userRepository, passwordEncoder, "user1", "user1@example.com", "password123", "USER");

        String adminToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "admin@example.com", "password123");

        // Find the user's ID via paginated search
        MvcResult searchResult = mockMvc.perform(get("/api/users?keyword=user1").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andReturn();
        JsonNode searchContent = mapper.readTree(searchResult.getResponse().getContentAsString()).path("data").path("content");
        Long userId = searchContent.get(0).path("id").asLong();

        // Disable
        MvcResult disableResult = mockMvc.perform(patch("/api/users/" + userId + "/enabled?enabled=false").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andReturn();
        JsonNode disabledData = mapper.readTree(disableResult.getResponse().getContentAsString()).path("data");
        assertThat(disabledData.path("enabled").asBoolean()).isFalse();

        // Verify disabled user cannot login
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("email", "user1@example.com", "password", "password123"))))
                .andExpect(status().isUnauthorized());

        // Re-enable
        MvcResult enableResult = mockMvc.perform(patch("/api/users/" + userId + "/enabled?enabled=true").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andReturn();
        JsonNode enabledData = mapper.readTree(enableResult.getResponse().getContentAsString()).path("data");
        assertThat(enabledData.path("enabled").asBoolean()).isTrue();
    }

    @Test
    void changePassword_successThenLoginWithNewPassword() throws Exception {
        TestDataUtil.createUser(userRepository, passwordEncoder, "user1", "user1@example.com", "password123", "USER");
        String token = TestDataUtil.loginAndGetToken(mockMvc, mapper, "user1@example.com", "password123");

        // Get user ID
        MvcResult meResult = mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        Long userId = mapper.readTree(meResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Change password
        Map<String, String> passwordPayload = Map.of("currentPassword", "password123", "newPassword", "newpass456");
        mockMvc.perform(put("/api/users/" + userId + "/password").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(passwordPayload)))
                .andExpect(status().isOk());

        // Login with new password
        String newToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "user1@example.com", "newpass456");
        assertThat(newToken).isNotNull();
    }

    @Test
    void changePassword_wrongCurrentPasswordReturns403() throws Exception {
        TestDataUtil.createUser(userRepository, passwordEncoder, "user1", "user1@example.com", "password123", "USER");
        String token = TestDataUtil.loginAndGetToken(mockMvc, mapper, "user1@example.com", "password123");

        MvcResult meResult = mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        Long userId = mapper.readTree(meResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        Map<String, String> passwordPayload = Map.of("currentPassword", "wrongpass", "newPassword", "newpass456");
        mockMvc.perform(put("/api/users/" + userId + "/password").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(passwordPayload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_cannotChangeOtherUserPassword() throws Exception {
        TestDataUtil.createUser(userRepository, passwordEncoder, "user1", "user1@example.com", "password123", "USER");
        TestDataUtil.createUser(userRepository, passwordEncoder, "user2", "user2@example.com", "password123", "USER");

        String token1 = TestDataUtil.loginAndGetToken(mockMvc, mapper, "user1@example.com", "password123");

        // Get user2's ID by logging in as user2 and calling /me
        String token2 = TestDataUtil.loginAndGetToken(mockMvc, mapper, "user2@example.com", "password123");
        MvcResult me2 = mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk()).andReturn();
        Long user2Id = mapper.readTree(me2.getResponse().getContentAsString()).path("data").path("id").asLong();

        // user1 tries to change user2's password — should be forbidden
        Map<String, String> passwordPayload = Map.of("currentPassword", "password123", "newPassword", "newpass456");
        mockMvc.perform(put("/api/users/" + user2Id + "/password").header("Authorization", "Bearer " + token1).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(passwordPayload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_withPaginationAndFilters() throws Exception {
        TestDataUtil.createUser(userRepository, passwordEncoder, "admin", "admin@example.com", "password123", "ADMIN");
        TestDataUtil.createUser(userRepository, passwordEncoder, "mgr1", "mgr1@example.com", "password123", "MANAGER");
        TestDataUtil.createUser(userRepository, passwordEncoder, "mgr2", "mgr2@example.com", "password123", "MANAGER");
        TestDataUtil.createUser(userRepository, passwordEncoder, "user1", "user1@example.com", "password123", "USER");

        String adminToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "admin@example.com", "password123");

        // Filter by role=MANAGER
        MvcResult roleResult = mockMvc.perform(get("/api/users?role=MANAGER").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andReturn();
        JsonNode roleContent = mapper.readTree(roleResult.getResponse().getContentAsString()).path("data").path("content");
        assertThat(roleContent.size()).isEqualTo(2);
        for (JsonNode node : roleContent) {
            assertThat(node.path("role").asText()).isEqualTo("MANAGER");
        }

        // Filter by keyword=mgr
        MvcResult kwResult = mockMvc.perform(get("/api/users?keyword=mgr").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andReturn();
        JsonNode kwContent = mapper.readTree(kwResult.getResponse().getContentAsString()).path("data").path("content");
        assertThat(kwContent.size()).isEqualTo(2);

        // Filter by enabled=true
        MvcResult enabledResult = mockMvc.perform(get("/api/users?enabled=true").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andReturn();
        JsonNode enabledContent = mapper.readTree(enabledResult.getResponse().getContentAsString()).path("data").path("content");
        assertThat(enabledContent.size()).isEqualTo(4);

        // Pagination: page 0, size 2
        MvcResult pageResult = mockMvc.perform(get("/api/users?page=0&size=2").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andReturn();
        JsonNode pageData = mapper.readTree(pageResult.getResponse().getContentAsString()).path("data");
        assertThat(pageData.path("size").asInt()).isEqualTo(2);
        assertThat(pageData.path("content").size()).isEqualTo(2);
        assertThat(pageData.path("totalElements").asInt()).isEqualTo(4);
    }

    @Test
    void nonAdminCannotAccessUserList() throws Exception {
        TestDataUtil.createUser(userRepository, passwordEncoder, "user1", "user1@example.com", "password123", "USER");
        String token = TestDataUtil.loginAndGetToken(mockMvc, mapper, "user1@example.com", "password123");

        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
