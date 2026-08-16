package com.telecom.campaign.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.telecom.campaign.common.enums.CampaignStatus;
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

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CampaignIntegrationTest {

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
                // delete campaigns first because of FK to users
                campaignRepository.deleteAll();
                userRepository.deleteAll();
    }

    @Test
    void adminAndManagerCanCreateCampaignAndStatusIsDraft() throws Exception{
        TestDataUtil.createUser(userRepository,passwordEncoder,"admin","admin@example.com","password123",Role.ADMIN.name());
        TestDataUtil.createUser(userRepository,passwordEncoder,"mgr","mgr@example.com","password123",Role.MANAGER.name());

        String adminToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "admin@example.com", "password123");
        String mgrToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "mgr@example.com", "password123");

        Map<String,Object> payloadAdmin = new java.util.HashMap<>();
        payloadAdmin.put("name", "Camp 1 admin " + System.nanoTime());
        payloadAdmin.put("description", "desc");
        payloadAdmin.put("startDate", LocalDateTime.now().plusDays(1).toString());
        payloadAdmin.put("endDate", LocalDateTime.now().plusDays(10).toString());

        Map<String,Object> payloadMgr = new java.util.HashMap<>();
        payloadMgr.put("name", "Camp 1 mgr " + System.nanoTime());
        payloadMgr.put("description", "desc");
        payloadMgr.put("startDate", LocalDateTime.now().plusDays(1).toString());
        payloadMgr.put("endDate", LocalDateTime.now().plusDays(10).toString());

        var r1 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payloadAdmin)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root1 = mapper.readTree(r1.getResponse().getContentAsString());
        assertThat(root1.path("data").path("status").asText()).isEqualTo(CampaignStatus.DRAFT.name());

        var r2 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payloadMgr)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root2 = mapper.readTree(r2.getResponse().getContentAsString());
        assertThat(root2.path("data").path("status").asText()).isEqualTo(CampaignStatus.DRAFT.name());
    }

    @Test
    void ownershipAndAuthorizationRules() throws Exception{
        TestDataUtil.createUser(userRepository,passwordEncoder,"admin","admin@example.com","password123",Role.ADMIN.name());
        TestDataUtil.createUser(userRepository,passwordEncoder,"mgr1","mgr1@example.com","password123",Role.MANAGER.name());
        TestDataUtil.createUser(userRepository,passwordEncoder,"mgr2","mgr2@example.com","password123",Role.MANAGER.name());
        TestDataUtil.createUser(userRepository,passwordEncoder,"user","user@example.com","password123",Role.USER.name());

        String adminToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "admin@example.com", "password123");
        String mgr1Token = TestDataUtil.loginAndGetToken(mockMvc, mapper, "mgr1@example.com", "password123");
        String mgr2Token = TestDataUtil.loginAndGetToken(mockMvc, mapper, "mgr2@example.com", "password123");
        String userToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "user@example.com", "password123");

        Map<String,Object> payload = Map.of(
                "name","OwnedCamp",
                "description","desc",
                "startDate", LocalDateTime.now().plusDays(1).toString(),
                "endDate", LocalDateTime.now().plusDays(10).toString()
        );

        // manager1 creates campaign
        var create = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgr1Token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = mapper.readTree(create.getResponse().getContentAsString());
        Long id = created.path("data").path("id").asLong();

        // admin can update
        Map<String,Object> updatePayload = Map.of("name","Updated","description","d","startDate",LocalDateTime.now().plusDays(2).toString(),"endDate",LocalDateTime.now().plusDays(5).toString());
        mockMvc.perform(put("/api/campaigns/"+id).header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk());

        // admin can delete
        var del = mockMvc.perform(delete("/api/campaigns/"+id).header("Authorization","Bearer "+adminToken)).andExpect(status().isOk()).andReturn();

        // recreate to test manager operations
        var create2 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgr1Token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn();
        Long id2 = mapper.readTree(create2.getResponse().getContentAsString()).path("data").path("id").asLong();

        // manager1 can update own
        mockMvc.perform(put("/api/campaigns/"+id2).header("Authorization","Bearer "+mgr1Token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk());

        // manager2 cannot update manager1's campaign
        mockMvc.perform(put("/api/campaigns/"+id2).header("Authorization","Bearer "+mgr2Token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(updatePayload)))
                .andExpect(status().isForbidden());

        // user cannot create or modify campaigns
        mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+userToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void statusTransitionValidAndInvalid() throws Exception{
        TestDataUtil.createUser(userRepository,passwordEncoder,"mgr","mgr@example.com","password123",Role.MANAGER.name());
        String mgrToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "mgr@example.com", "password123");

        java.util.function.Supplier<Map<String,Object>> newPayload = () -> {
            Map<String,Object> p = new java.util.HashMap<>();
            p.put("name", "StatCamp-" + System.nanoTime());
            p.put("description", "desc");
            p.put("startDate", LocalDateTime.now().plusDays(1).toString());
            p.put("endDate", LocalDateTime.now().plusDays(10).toString());
            return p;
        };

        // DRAFT -> ACTIVE succeeds
        var c1 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(newPayload.get())))
                .andExpect(status().isCreated()).andReturn();
        Long id1 = mapper.readTree(c1.getResponse().getContentAsString()).path("data").path("id").asLong();
        mockMvc.perform(patch("/api/campaigns/"+id1+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.ACTIVE))))
                .andExpect(status().isOk());

        // DRAFT -> CANCELLED succeeds
        var c2 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(newPayload.get())))
                .andExpect(status().isCreated()).andReturn();
        Long id2 = mapper.readTree(c2.getResponse().getContentAsString()).path("data").path("id").asLong();
        mockMvc.perform(patch("/api/campaigns/"+id2+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.CANCELLED))))
                .andExpect(status().isOk());

        // DRAFT -> PAUSED fails
        var c3 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(newPayload.get())))
                .andExpect(status().isCreated()).andReturn();
        Long id3 = mapper.readTree(c3.getResponse().getContentAsString()).path("data").path("id").asLong();
        mockMvc.perform(patch("/api/campaigns/"+id3+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.PAUSED))))
                .andExpect(status().isBadRequest());

        // ACTIVE -> PAUSED, COMPLETED, CANCELLED
        var c4 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(newPayload.get())))
                .andExpect(status().isCreated()).andReturn();
        Long id4 = mapper.readTree(c4.getResponse().getContentAsString()).path("data").path("id").asLong();
        // make ACTIVE
        mockMvc.perform(patch("/api/campaigns/"+id4+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.ACTIVE))))
                .andExpect(status().isOk());
        // ACTIVE -> PAUSED
        mockMvc.perform(patch("/api/campaigns/"+id4+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.PAUSED))))
                .andExpect(status().isOk());
        // PAUSED -> ACTIVE
        mockMvc.perform(patch("/api/campaigns/"+id4+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.ACTIVE))))
                .andExpect(status().isOk());
        // ACTIVE -> COMPLETED
        mockMvc.perform(patch("/api/campaigns/"+id4+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.COMPLETED))))
                .andExpect(status().isOk());
        // COMPLETED cannot transition (to ACTIVE)
        mockMvc.perform(patch("/api/campaigns/"+id4+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.ACTIVE))))
                .andExpect(status().isBadRequest());

        // PAUSED -> CANCELLED succeeds and -> COMPLETED fails
        var c5 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(newPayload.get())))
                .andExpect(status().isCreated()).andReturn();
        Long id5 = mapper.readTree(c5.getResponse().getContentAsString()).path("data").path("id").asLong();
        // ACTIVE
        mockMvc.perform(patch("/api/campaigns/"+id5+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.ACTIVE))))
                .andExpect(status().isOk());
        // ACTIVE -> PAUSED
        mockMvc.perform(patch("/api/campaigns/"+id5+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.PAUSED))))
                .andExpect(status().isOk());
        // PAUSED -> ACTIVE
        mockMvc.perform(patch("/api/campaigns/"+id5+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.ACTIVE))))
                .andExpect(status().isOk());
        // make PAUSED again
        mockMvc.perform(patch("/api/campaigns/"+id5+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.PAUSED))))
                .andExpect(status().isOk());
        // PAUSED -> CANCELLED
        mockMvc.perform(patch("/api/campaigns/"+id5+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.CANCELLED))))
                .andExpect(status().isOk());
        // CANCELLED cannot transition
        mockMvc.perform(patch("/api/campaigns/"+id5+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.ACTIVE))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filteringPaginationAndSorting() throws Exception{
                TestDataUtil.createUser(userRepository,passwordEncoder,"mgr","mgr@example.com","password123",Role.MANAGER.name());
                String mgrToken = TestDataUtil.loginAndGetToken(mockMvc, mapper, "mgr@example.com", "password123");

                // create campaigns with varying names, descriptions and dates
                Map<String,Object> p1 = Map.of("name","Alpha","description","first","startDate",LocalDateTime.now().plusDays(1).toString(),"endDate",LocalDateTime.now().plusDays(5).toString());
                Map<String,Object> p2 = Map.of("name","BetaSpecial","description","second special","startDate",LocalDateTime.now().plusDays(2).toString(),"endDate",LocalDateTime.now().plusDays(6).toString());
                Map<String,Object> p3 = Map.of("name","Gamma","description","third","startDate",LocalDateTime.now().plusDays(3).toString(),"endDate",LocalDateTime.now().plusDays(7).toString());
                Map<String,Object> p4 = Map.of("name","Delta","description","fourth","startDate",LocalDateTime.now().plusDays(4).toString(),"endDate",LocalDateTime.now().plusDays(8).toString());

                var c1 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(p1))).andExpect(status().isCreated()).andReturn();
                var c2 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(p2))).andExpect(status().isCreated()).andReturn();
                var c3 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(p3))).andExpect(status().isCreated()).andReturn();
                var c4 = mockMvc.perform(post("/api/campaigns").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(p4))).andExpect(status().isCreated()).andReturn();

                Long id1 = mapper.readTree(c1.getResponse().getContentAsString()).path("data").path("id").asLong();
                Long id2 = mapper.readTree(c2.getResponse().getContentAsString()).path("data").path("id").asLong();
                Long id3 = mapper.readTree(c3.getResponse().getContentAsString()).path("data").path("id").asLong();
                Long id4 = mapper.readTree(c4.getResponse().getContentAsString()).path("data").path("id").asLong();

                // set status of some campaigns
                mockMvc.perform(patch("/api/campaigns/"+id2+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.ACTIVE)))).andExpect(status().isOk());
                mockMvc.perform(patch("/api/campaigns/"+id3+"/status").header("Authorization","Bearer "+mgrToken).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",CampaignStatus.CANCELLED)))).andExpect(status().isOk());

                // keyword filter (name or description)
                var kwRes = mockMvc.perform(get("/api/campaigns?keyword=Special").header("Authorization","Bearer "+mgrToken)).andExpect(status().isOk()).andReturn();
                JsonNode kwRoot = mapper.readTree(kwRes.getResponse().getContentAsString());
                JsonNode kwContent = kwRoot.path("data").path("content");
                assertThat(kwContent.isArray()).isTrue();
                assertThat(kwContent.size()).isGreaterThanOrEqualTo(1);
                assertThat(kwContent.get(0).path("name").asText()).contains("BetaSpecial");

                // status filter
                var stRes = mockMvc.perform(get("/api/campaigns?status=ACTIVE").header("Authorization","Bearer "+mgrToken)).andExpect(status().isOk()).andReturn();
                JsonNode stContent = mapper.readTree(stRes.getResponse().getContentAsString()).path("data").path("content");
                assertThat(stContent.size()).isGreaterThanOrEqualTo(1);

                // startDate filter: campaigns starting on or after day 3
                String startDateParam = LocalDateTime.now().plusDays(3).toString();
                var sdRes = mockMvc.perform(get("/api/campaigns?startDate="+startDateParam).header("Authorization","Bearer "+mgrToken)).andExpect(status().isOk()).andReturn();
                JsonNode sdContent = mapper.readTree(sdRes.getResponse().getContentAsString()).path("data").path("content");
                assertThat(sdContent.size()).isGreaterThanOrEqualTo(1);

                // endDate filter: campaigns ending on or before day 6
                String endDateParam = LocalDateTime.now().plusDays(6).toString();
                var edRes = mockMvc.perform(get("/api/campaigns?endDate="+endDateParam).header("Authorization","Bearer "+mgrToken)).andExpect(status().isOk()).andReturn();
                JsonNode edContent = mapper.readTree(edRes.getResponse().getContentAsString()).path("data").path("content");
                assertThat(edContent.size()).isGreaterThanOrEqualTo(1);

                // pagination & sorting: page size 2 sorted by name desc
                var pageRes = mockMvc.perform(get("/api/campaigns?page=0&size=2&sort=name,desc").header("Authorization","Bearer "+mgrToken)).andExpect(status().isOk()).andReturn();
                JsonNode page = mapper.readTree(pageRes.getResponse().getContentAsString()).path("data");
                assertThat(page.path("size").asInt()).isEqualTo(2);
                JsonNode contents = page.path("content");
                assertThat(contents.isArray()).isTrue();
                if(contents.size() > 1){
                        String firstName = contents.get(0).path("name").asText();
                        String secondName = contents.get(1).path("name").asText();
                        assertThat(firstName.compareTo(secondName)).isGreaterThanOrEqualTo(0);
                }
    }
}
