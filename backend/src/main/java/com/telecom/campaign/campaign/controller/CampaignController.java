package com.telecom.campaign.campaign.controller;

import com.telecom.campaign.campaign.dto.CampaignRequest;
import com.telecom.campaign.campaign.dto.CampaignResponse;
import com.telecom.campaign.campaign.dto.CampaignStatusRequest;
import com.telecom.campaign.campaign.service.CampaignService;
import com.telecom.campaign.common.dto.ApiResponse;
import com.telecom.campaign.common.enums.CampaignStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService){
        this.campaignService = campaignService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(@Valid @RequestBody CampaignRequest campaignRequest){
        CampaignResponse campaignResponse = campaignService.createCampaign(campaignRequest);
        ApiResponse<CampaignResponse> response = ApiResponse.<CampaignResponse>builder().success(true).statusCode(201).message("Campaign Created Successfully").data(campaignResponse).build();
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaign(@PathVariable Long id){
        CampaignResponse campaignResponse = campaignService.getCampaign(id);
        ApiResponse<CampaignResponse> response = ApiResponse.<CampaignResponse>builder().success(true).statusCode(200).message("Campaign Fetched Successfully").data(campaignResponse).build();
        return ResponseEntity.status(200).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> getAllCampaign(@RequestParam(required = false) CampaignStatus status, @RequestParam(required = false) String keyword, @RequestParam(required = false) LocalDateTime startDate, @RequestParam(required = false) LocalDateTime endDate, Pageable pageable){
        Page<CampaignResponse> campaignResponse = campaignService.getCampaigns(status, keyword,startDate, endDate, pageable);
        ApiResponse<Page<CampaignResponse>> response = ApiResponse.<Page<CampaignResponse>>builder().success(true).statusCode(200).message("Campaigns Fetched Successfully").data(campaignResponse).build();
        return ResponseEntity.status(200).body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaign(@PathVariable Long id,@Valid @RequestBody CampaignRequest campaignRequest){
        CampaignResponse campaignResponse = campaignService.updateCampaign(id,campaignRequest);
        ApiResponse<CampaignResponse> response = ApiResponse.<CampaignResponse>builder().success(true).statusCode(200).message("Campaign Updated Successfully").data(campaignResponse).build();
        return ResponseEntity.status(200).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable Long id){
        campaignService.deleteCampaign(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder().success(true).statusCode(200).message("Campaign Deleted Successfully").data(null).build();
        return ResponseEntity.status(200).body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaignStatus(@PathVariable Long id, @Valid @RequestBody CampaignStatusRequest campaignStatusRequest){
        CampaignResponse campaignResponse = campaignService.updateCampaignStatus(id,campaignStatusRequest.getStatus());
        ApiResponse<CampaignResponse> response = ApiResponse.<CampaignResponse>builder().success(true).statusCode(200).message("Campaign Status Updated").data(campaignResponse).build();
        return ResponseEntity.ok(response);
    }
}
