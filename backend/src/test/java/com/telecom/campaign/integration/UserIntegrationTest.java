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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserIntegrationTest {

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
        campaignRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void publicRegistrationCreatesUser() throws Exception{
        Map<String,String> payload = Map.of("username","janedoe","email","jane@example.com","password","password123");
        var result = mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = mapper.readTree(result.getResponse().getContentAsString());
        JsonNode roleNode = root.path("data").path("role");
        assertThat(roleNode.asText()).isEqualTo(Role.USER.name());
    }

    @Test
    void adminCanCreateManagerAndUserButNotAdmin() throws Exception{
        TestDataUtil.createUser(userRepository,passwordEncoder,"admin","admin@example.com","password123","ADMIN");
        String adminToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "admin@example.com", "password123");

        // create manager
        Map<String,Object> mgrPayload = Map.of("username","mgr","email","mgr@example.com","password","pass1234","role",Role.MANAGER);
        var r1 = mockMvc.perform(post("/api/users").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(mgrPayload)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root1 = mapper.readTree(r1.getResponse().getContentAsString());
        assertThat(root1.path("data").path("role").asText()).isEqualTo(Role.MANAGER.name());

        // create user
        Map<String,Object> userPayload = Map.of("username","user1","email","user1@example.com","password","pass1234","role",Role.USER);
        var r2 = mockMvc.perform(post("/api/users").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(userPayload)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root2 = mapper.readTree(r2.getResponse().getContentAsString());
        assertThat(root2.path("data").path("role").asText()).isEqualTo(Role.USER.name());

        // cannot create ADMIN
        Map<String,Object> adminPayload = Map.of("username","bad","email","bad@example.com","password","pass1234","role",Role.ADMIN);
        mockMvc.perform(post("/api/users").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(adminPayload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonAdminCannotAccessCreateUserEndpoint() throws Exception{
        TestDataUtil.createUser(userRepository,passwordEncoder,"bob","bob@example.com","password123","USER");
        String token = TestDataUtil.loginAndGetToken(mockMvc, mapper, "bob@example.com", "password123");

        Map<String,Object> payload = Map.of("username","xuser","email","x@example.com","password","pass1234","role",Role.USER);
        mockMvc.perform(post("/api/users").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }
}
