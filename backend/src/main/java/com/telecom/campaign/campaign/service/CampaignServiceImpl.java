package com.telecom.campaign.campaign.service;

import com.telecom.campaign.campaign.dto.CampaignRequest;
import com.telecom.campaign.campaign.dto.CampaignResponse;
import com.telecom.campaign.campaign.entity.Campaign;
import com.telecom.campaign.campaign.mapper.CampaignMapper;
import com.telecom.campaign.campaign.repository.CampaignRepository;
import com.telecom.campaign.campaign.specification.CampaignSpecification;
import com.telecom.campaign.common.enums.CampaignStatus;
import com.telecom.campaign.common.enums.Role;
import com.telecom.campaign.exception.InvalidCampaignStatusException;
import com.telecom.campaign.exception.ResourceNotFoundException;
import com.telecom.campaign.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class CampaignServiceImpl implements CampaignService{

    private final CampaignMapper campaignMapper;
    private final CampaignRepository campaignRepository;

    public CampaignServiceImpl(CampaignMapper campaignMapper, CampaignRepository campaignRepository){
        this.campaignMapper = campaignMapper;
        this.campaignRepository = campaignRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Override
    public CampaignResponse createCampaign(CampaignRequest campaignRequest){
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Campaign campaign = Campaign.builder().name(campaignRequest.getName()).description(campaignRequest.getDescription()).status(CampaignStatus.DRAFT).startDate(campaignRequest.getStartDate()).endDate(campaignRequest.getEndDate()).manager(user).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        Campaign savedCampaign = campaignRepository.save(campaign);
        return campaignMapper.toResponse(savedCampaign);
    }

    @Override
    public CampaignResponse getCampaign(Long id){
        Campaign campaign = campaignRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Campaign not found"));
        return campaignMapper.toResponse(campaign);
    }

    @Override
    public Page<CampaignResponse> getAllCampaign(Pageable pageable){
        Page<Campaign> campaigns = campaignRepository.findAll(pageable);
        return campaigns.map(campaignMapper::toResponse);
    }

    @Override
    public  Page<CampaignResponse> getCampaigns(CampaignStatus status, String keyword,LocalDateTime startDate, LocalDateTime endDate, Pageable pageable){
        Specification<Campaign> specification = null;

        if(status != null){
            specification = CampaignSpecification.hasStatus(status);
        }

        if(keyword != null && !keyword.isBlank()){
            Specification<Campaign> keywordSpec = CampaignSpecification.hasKeyword(keyword);
            if(specification == null){
                specification = keywordSpec;
            }else{
                specification = specification.and(keywordSpec);
            }
        }

        if(startDate !=null){
            Specification<Campaign> startDateSpec = CampaignSpecification.hasStartDateAfterOrEqual(startDate);
            if(specification == null){
                specification = startDateSpec;
            }else{
                specification = specification.and(startDateSpec);
            }
        }

        if(endDate!= null){
            Specification<Campaign> endDateSpec = CampaignSpecification.hasEndDateBeforeOrEqual(endDate);
            if(specification == null){
                specification = endDateSpec;
            }else{
                specification = specification.and(endDateSpec);
            }
        }

        Page<Campaign> campaigns = campaignRepository.findAll(specification, pageable);
        return campaigns.map(campaignMapper::toResponse);
    }

    @Override
    public CampaignResponse updateCampaign(Long id, CampaignRequest campaignRequest){
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Campaign campaign = campaignRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Campaign Not Found"));

        checkCampaignAccess(currentUser,campaign);

        campaign.setName(campaignRequest.getName());
        campaign.setDescription(campaignRequest.getDescription());
        campaign.setStartDate(campaignRequest.getStartDate());
        campaign.setEndDate(campaignRequest.getEndDate());
        campaign.setUpdatedAt(LocalDateTime.now());
        Campaign updatedCampaign = campaignRepository.save(campaign);
        return campaignMapper.toResponse(updatedCampaign);
    }


    @Override
    public void deleteCampaign(Long id){
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Campaign campaign = campaignRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Campaign not Found"));
        checkCampaignAccess(currentUser, campaign);
        campaignRepository.delete(campaign);
    }

    private void checkCampaignAccess(User currentUser, Campaign campaign){

        if(currentUser.getRole() == Role.ADMIN){
            return;
        }

        if(currentUser.getRole() == Role.MANAGER){
            if(campaign.getManager().getId().equals(currentUser.getId())){
                return;
            }
        }

        throw new AccessDeniedException(
                "You are not authorized to modify this campaign"
        );
    }

    @Override
    @Transactional
    public CampaignResponse updateCampaignStatus(Long id, CampaignStatus status){
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Campaign campaign = campaignRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Campaign not Found"));
        checkCampaignAccess(currentUser,campaign);
        validateStatusTransition(campaign.getStatus(), status);
        campaign.setStatus(status);
        campaign.setUpdatedAt(LocalDateTime.now());
        return campaignMapper.toResponse(campaign);
    }

    private void validateStatusTransition(CampaignStatus currentStatus, CampaignStatus newStatus){
        if(currentStatus == CampaignStatus.COMPLETED || currentStatus == CampaignStatus.CANCELLED){
            throw new InvalidCampaignStatusException("Campaign is already "+currentStatus);
        }

        if(currentStatus == CampaignStatus.DRAFT && newStatus != CampaignStatus.ACTIVE && newStatus != CampaignStatus.CANCELLED){
            throw new InvalidCampaignStatusException("Draft campaign can only be activated or cancelled");
        }

        if(currentStatus == CampaignStatus.ACTIVE && newStatus != CampaignStatus.PAUSED && newStatus != CampaignStatus.CANCELLED && newStatus != CampaignStatus.COMPLETED){
            throw new InvalidCampaignStatusException("Active campaign can only be paused, completed or cancelled");
        }

        if(currentStatus == CampaignStatus.PAUSED && newStatus != CampaignStatus.ACTIVE && newStatus != CampaignStatus.CANCELLED){
            throw new InvalidCampaignStatusException("Paused campaign can only be activated or cancelled");
        }
    }
}
