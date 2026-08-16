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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ErrorHandlingIntegrationTest {

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
    void validationErrorReturns400() throws Exception{
        // missing email
        Map<String,String> payload = Map.of("username","alice","password","password123");
        var res = mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload))).andExpect(status().isBadRequest()).andReturn();
        JsonNode root = mapper.readTree(res.getResponse().getContentAsString());
        assertThat(root.path("statusCode").asInt()).isEqualTo(400);
        assertThat(root.path("data").isObject()).isTrue();
    }

    @Test
    void unauthenticatedRequestsReturn401() throws Exception{
        mockMvc.perform(post("/api/campaigns").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    void forbiddenReturns403() throws Exception{
        TestDataUtil.createUser(userRepository,passwordEncoder,"bob","bob@example.com","password123", Role.USER.name());
        String token = TestDataUtil.loginAndGetToken(mockMvc, mapper, "bob@example.com", "password123");
        mockMvc.perform(post("/api/users").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("username","xuser","email","x@e.com","password","pass123","role",Role.USER)))).andExpect(status().isForbidden());
    }

    @Test
    void notFoundReturns404() throws Exception{
        TestDataUtil.createUser(userRepository,passwordEncoder,"mgr","mgr@example.com","password123",Role.MANAGER.name());
        String token = TestDataUtil.loginAndGetToken(mockMvc, mapper, "mgr@example.com", "password123");
        mockMvc.perform(get("/api/campaigns/99999").header("Authorization","Bearer "+token)).andExpect(status().isNotFound());
    }

    @Test
    void duplicateEmailReturns409() throws Exception{
        Map<String,String> payload = Map.of("username","dupuser","email","dup@example.com","password","password123");
        mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload))).andExpect(status().isConflict());
    }
}
