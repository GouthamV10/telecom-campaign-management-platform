package com.telecom.campaign.campaign.dto;

import com.telecom.campaign.common.enums.CampaignStatus;
import com.telecom.campaign.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignResponse {

    private Long id;

    private String name;

    private String description;

    private CampaignStatus status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Long managerId;

    private String managerName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
