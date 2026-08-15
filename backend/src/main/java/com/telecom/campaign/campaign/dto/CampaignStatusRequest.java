package com.telecom.campaign.campaign.dto;

import com.telecom.campaign.common.enums.CampaignStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatusRequest {

    @NotNull
    private CampaignStatus status;
}
