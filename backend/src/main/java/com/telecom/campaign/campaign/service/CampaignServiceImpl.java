package com.telecom.campaign.campaign.service;

import com.telecom.campaign.campaign.dto.CampaignRequest;
import com.telecom.campaign.campaign.dto.CampaignResponse;
import com.telecom.campaign.campaign.entity.Campaign;
import com.telecom.campaign.campaign.mapper.CampaignMapper;
import com.telecom.campaign.campaign.repository.CampaignRepository;
import com.telecom.campaign.campaign.specification.CampaignSpecification;
import com.telecom.campaign.common.enums.CampaignStatus;
import com.telecom.campaign.exception.ResourceNotFoundException;
import com.telecom.campaign.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class CampaignServiceImpl implements CampaignService{

    private final CampaignMapper campaignMapper;
    private final CampaignRepository campaignRepository;

    public CampaignServiceImpl(CampaignMapper campaignMapper, CampaignRepository campaignRepository){
        this.campaignMapper = campaignMapper;
        this.campaignRepository = campaignRepository;
    }

    @Override
    public CampaignResponse createCampaign(CampaignRequest campaignRequest){
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Campaign campaign = Campaign.builder().name(campaignRequest.getName()).description(campaignRequest.getDescription()).status(campaignRequest.getStatus()).startDate(campaignRequest.getStartDate()).endDate(campaignRequest.getEndDate()).manager(user).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
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
        Campaign campaign = campaignRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Campaign Not Found"));
        campaign.setName(campaignRequest.getName());
        campaign.setDescription(campaignRequest.getDescription());
        campaign.setStatus(campaignRequest.getStatus());
        campaign.setStartDate(campaignRequest.getStartDate());
        campaign.setEndDate(campaignRequest.getEndDate());
        campaign.setUpdatedAt(LocalDateTime.now());
        Campaign updatedCampaign = campaignRepository.save(campaign);
        return campaignMapper.toResponse(updatedCampaign);
    }

    @Override
    public void deleteCampaign(Long id){
        Campaign campaign = campaignRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Campaign not Found"));
        campaignRepository.delete(campaign);
    }

}
