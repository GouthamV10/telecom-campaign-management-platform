package com.telecom.campaign.unit;

import com.telecom.campaign.campaign.dto.CampaignRequest;
import com.telecom.campaign.campaign.dto.CampaignResponse;
import com.telecom.campaign.campaign.entity.Campaign;
import com.telecom.campaign.campaign.mapper.CampaignMapper;
import com.telecom.campaign.campaign.repository.CampaignRepository;
import com.telecom.campaign.campaign.service.CampaignServiceImpl;
import com.telecom.campaign.common.enums.CampaignStatus;
import com.telecom.campaign.common.enums.Role;
import com.telecom.campaign.exception.InvalidCampaignStatusException;
import com.telecom.campaign.exception.ResourceNotFoundException;
import com.telecom.campaign.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    @Mock
    private CampaignMapper campaignMapper;

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    private User manager;
    private User otherManager;
    private User admin;
    private Campaign testCampaign;

    @BeforeEach
    void setUp() {
        manager = new User();
        manager.setId(1L);
        manager.setUsername("manager1");
        manager.setEmail("mgr1@example.com");
        manager.setRole(Role.MANAGER);
        manager.setEnabled(true);

        otherManager = new User();
        otherManager.setId(2L);
        otherManager.setUsername("manager2");
        otherManager.setEmail("mgr2@example.com");
        otherManager.setRole(Role.MANAGER);
        otherManager.setEnabled(true);

        admin = new User();
        admin.setId(3L);
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);

        testCampaign = Campaign.builder()
                .id(1L)
                .name("Test Campaign")
                .description("Test Description")
                .status(CampaignStatus.DRAFT)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .manager(manager)
                .build();
    }

    private void setSecurityContext(User user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, java.util.List.of()));
        SecurityContextHolder.setContext(context);
    }

    @Test
    void getCampaign_returnsResponseWhenFound() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignMapper.toResponse(testCampaign)).thenReturn(
                CampaignResponse.builder().id(1L).name("Test Campaign").status(CampaignStatus.DRAFT).build()
        );

        CampaignResponse response = campaignService.getCampaign(1L);

        assertThat(response.getName()).isEqualTo("Test Campaign");
    }

    @Test
    void getCampaign_throwsWhenNotFound() {
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.getCampaign(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCampaign_ownerManagerCanUpdate() {
        setSecurityContext(manager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignMapper.toResponse(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            return CampaignResponse.builder().id(c.getId()).name(c.getName()).build();
        });

        CampaignRequest request = new CampaignRequest();
        request.setName("Updated Name");
        request.setDescription("Updated Desc");
        request.setStartDate(LocalDateTime.now().plusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(5));

        CampaignResponse response = campaignService.updateCampaign(1L, request);

        assertThat(response.getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateCampaign_otherManagerCannotUpdate() {
        setSecurityContext(otherManager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        CampaignRequest request = new CampaignRequest();
        request.setName("Updated Name");
        request.setDescription("Updated Desc");
        request.setStartDate(LocalDateTime.now().plusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(5));

        assertThatThrownBy(() -> campaignService.updateCampaign(1L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateCampaign_adminCanUpdateAny() {
        setSecurityContext(admin);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignMapper.toResponse(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            return CampaignResponse.builder().id(c.getId()).name(c.getName()).build();
        });

        CampaignRequest request = new CampaignRequest();
        request.setName("Admin Updated");
        request.setDescription("Admin Desc");
        request.setStartDate(LocalDateTime.now().plusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(5));

        CampaignResponse response = campaignService.updateCampaign(1L, request);

        assertThat(response.getName()).isEqualTo("Admin Updated");
    }

    @Test
    void deleteCampaign_ownerCanDelete() {
        setSecurityContext(manager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        campaignService.deleteCampaign(1L);

        verify(campaignRepository).delete(testCampaign);
    }

    @Test
    void deleteCampaign_otherManagerCannotDelete() {
        setSecurityContext(otherManager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        assertThatThrownBy(() -> campaignService.deleteCampaign(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateCampaignStatus_draftToActiveSucceeds() {
        setSecurityContext(manager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignMapper.toResponse(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            return CampaignResponse.builder().id(c.getId()).status(c.getStatus()).build();
        });

        CampaignResponse response = campaignService.updateCampaignStatus(1L, CampaignStatus.ACTIVE);

        assertThat(response.getStatus()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void updateCampaignStatus_draftToPausedFails() {
        setSecurityContext(manager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        assertThatThrownBy(() -> campaignService.updateCampaignStatus(1L, CampaignStatus.PAUSED))
                .isInstanceOf(InvalidCampaignStatusException.class);
    }

    @Test
    void updateCampaignStatus_completedCannotTransition() {
        testCampaign.setStatus(CampaignStatus.COMPLETED);
        setSecurityContext(manager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        assertThatThrownBy(() -> campaignService.updateCampaignStatus(1L, CampaignStatus.ACTIVE))
                .isInstanceOf(InvalidCampaignStatusException.class);
    }

    @Test
    void updateCampaignStatus_cancelledCannotTransition() {
        testCampaign.setStatus(CampaignStatus.CANCELLED);
        setSecurityContext(manager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        assertThatThrownBy(() -> campaignService.updateCampaignStatus(1L, CampaignStatus.ACTIVE))
                .isInstanceOf(InvalidCampaignStatusException.class);
    }

    @Test
    void updateCampaignStatus_activeToPausedSucceeds() {
        testCampaign.setStatus(CampaignStatus.ACTIVE);
        setSecurityContext(manager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignMapper.toResponse(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            return CampaignResponse.builder().id(c.getId()).status(c.getStatus()).build();
        });

        CampaignResponse response = campaignService.updateCampaignStatus(1L, CampaignStatus.PAUSED);

        assertThat(response.getStatus()).isEqualTo(CampaignStatus.PAUSED);
    }

    @Test
    void updateCampaignStatus_activeToCompletedSucceeds() {
        testCampaign.setStatus(CampaignStatus.ACTIVE);
        setSecurityContext(manager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignMapper.toResponse(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            return CampaignResponse.builder().id(c.getId()).status(c.getStatus()).build();
        });

        CampaignResponse response = campaignService.updateCampaignStatus(1L, CampaignStatus.COMPLETED);

        assertThat(response.getStatus()).isEqualTo(CampaignStatus.COMPLETED);
    }

    @Test
    void updateCampaignStatus_pausedToActiveSucceeds() {
        testCampaign.setStatus(CampaignStatus.PAUSED);
        setSecurityContext(manager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));
        when(campaignMapper.toResponse(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            return CampaignResponse.builder().id(c.getId()).status(c.getStatus()).build();
        });

        CampaignResponse response = campaignService.updateCampaignStatus(1L, CampaignStatus.ACTIVE);

        assertThat(response.getStatus()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void updateCampaignStatus_pausedToCompletedFails() {
        testCampaign.setStatus(CampaignStatus.PAUSED);
        setSecurityContext(manager);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(testCampaign));

        assertThatThrownBy(() -> campaignService.updateCampaignStatus(1L, CampaignStatus.COMPLETED))
                .isInstanceOf(InvalidCampaignStatusException.class);
    }
}
