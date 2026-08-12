package com.telecom.campaign.campaign.service;

import com.telecom.campaign.campaign.dto.CampaignRequest;
import com.telecom.campaign.campaign.dto.CampaignResponse;
import com.telecom.campaign.campaign.entity.Campaign;
import com.telecom.campaign.campaign.mapper.CampaignMapper;
import com.telecom.campaign.campaign.repository.CampaignRepository;
import com.telecom.campaign.exception.ResourceNotFoundException;
import com.telecom.campaign.user.entity.User;
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
    public List<CampaignResponse> getAllCampaign(){
        List<Campaign> campaigns = campaignRepository.findAll();
        return campaigns.stream().map(campaignMapper::toResponse).toList();
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
