package com.telecom.campaign.campaign.service;

import com.telecom.campaign.campaign.dto.CampaignRequest;
import com.telecom.campaign.campaign.dto.CampaignResponse;

import java.util.List;

public interface CampaignService {

    CampaignResponse createCampaign(CampaignRequest campaignRequest);

    CampaignResponse getCampaign(Long id);

    List<CampaignResponse> getAllCampaign();

    CampaignResponse updateCampaign(Long id, CampaignRequest campaignRequest);

    void deleteCampaign(Long id);
}
