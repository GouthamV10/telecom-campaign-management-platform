package com.telecom.campaign.campaign.service;

import com.telecom.campaign.campaign.dto.CampaignRequest;
import com.telecom.campaign.campaign.dto.CampaignResponse;
import com.telecom.campaign.common.enums.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CampaignService {

    CampaignResponse createCampaign(CampaignRequest campaignRequest);

    CampaignResponse getCampaign(Long id);

    Page<CampaignResponse> getAllCampaign(Pageable pageable);

    Page<CampaignResponse> getCampaigns(CampaignStatus status, String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    CampaignResponse updateCampaign(Long id, CampaignRequest campaignRequest);

    void deleteCampaign(Long id);

    CampaignResponse updateCampaignStatus(Long id, CampaignStatus status);
}
