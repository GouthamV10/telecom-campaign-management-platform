package com.telecom.campaign.campaign.mapper;

import com.telecom.campaign.campaign.dto.CampaignResponse;
import com.telecom.campaign.campaign.entity.Campaign;
import com.telecom.campaign.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CampaignMapper {


    public CampaignResponse toResponse(Campaign campaign){
        User user = campaign.getManager();
        return CampaignResponse.builder().id(campaign.getId()).name(campaign.getName()).description(campaign.getDescription()).status(campaign.getStatus()).startDate(campaign.getStartDate()).endDate(campaign.getEndDate()).managerId(user.getId()).managerName(user.getUsername()).createdAt(campaign.getCreatedAt()).updatedAt(campaign.getUpdatedAt()).build();
    }
}
