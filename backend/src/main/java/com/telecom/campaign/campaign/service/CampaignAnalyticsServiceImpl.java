package com.telecom.campaign.campaign.service;

import com.telecom.campaign.campaign.dto.CampaignAnalyticsResponse;
import com.telecom.campaign.campaign.dto.CampaignResponse;
import com.telecom.campaign.campaign.entity.Campaign;
import com.telecom.campaign.campaign.mapper.CampaignMapper;
import com.telecom.campaign.campaign.repository.CampaignRepository;
import com.telecom.campaign.common.enums.CampaignStatus;
import com.telecom.campaign.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CampaignAnalyticsServiceImpl implements CampaignAnalyticsService {

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;

    public CampaignAnalyticsServiceImpl(CampaignRepository campaignRepository, CampaignMapper campaignMapper) {
        this.campaignRepository = campaignRepository;
        this.campaignMapper = campaignMapper;
    }

    @Override
    public CampaignAnalyticsResponse getCampaignAnalytics(Long campaignId) {
        log.info("Fetching analytics for campaign id={}", campaignId);
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        LocalDateTime now = LocalDateTime.now();
        long totalDays = calculateDays(campaign.getStartDate(), campaign.getEndDate());
        long elapsedDays = calculateDays(campaign.getStartDate(), now);
        long remainingDays = calculateDays(now, campaign.getEndDate());

        double progress = totalDays > 0 ? Math.min(100.0, (elapsedDays * 100.0) / totalDays) : 0;
        boolean overdue = campaign.getStatus() != CampaignStatus.COMPLETED
                && campaign.getStatus() != CampaignStatus.CANCELLED
                && now.isAfter(campaign.getEndDate());

        return CampaignAnalyticsResponse.builder()
                .campaignId(campaign.getId())
                .campaignName(campaign.getName())
                .status(campaign.getStatus().name())
                .managerName(campaign.getManager().getUsername())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .durationDays(elapsedDays)
                .totalDurationDays(totalDays)
                .progressPercentage(Math.round(progress * 100.0) / 100.0)
                .daysRemaining(Math.max(0, remainingDays))
                .isOverdue(overdue)
                .statusHistory(List.of())
                .build();
    }

    @Override
    public List<CampaignResponse> getExpiringCampaigns(int days) {
        log.info("Fetching campaigns expiring within {} days", days);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);

        List<Campaign> all = campaignRepository.findAll();
        return all.stream()
                .filter(c -> c.getStatus() == CampaignStatus.ACTIVE || c.getStatus() == CampaignStatus.DRAFT)
                .filter(c -> c.getEndDate() != null && c.getEndDate().isAfter(now) && c.getEndDate().isBefore(threshold))
                .map(campaignMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countByStatus(CampaignStatus status) {
        return campaignRepository.countByStatus(status);
    }

    @Override
    public double getCompletionRate() {
        long total = campaignRepository.count();
        if (total == 0) return 0;
        long completed = campaignRepository.countByStatus(CampaignStatus.COMPLETED);
        return Math.round((completed * 100.0 / total) * 100.0) / 100.0;
    }

    @Override
    public double getActiveCampaignsPercentage() {
        long total = campaignRepository.count();
        if (total == 0) return 0;
        long active = campaignRepository.countByStatus(CampaignStatus.ACTIVE);
        return Math.round((active * 100.0 / total) * 100.0) / 100.0;
    }

    private long calculateDays(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }
}
