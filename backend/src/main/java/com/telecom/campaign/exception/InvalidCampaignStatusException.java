package com.telecom.campaign.exception;

public class InvalidCampaignStatusException extends RuntimeException {
    public InvalidCampaignStatusException(String message) {
        super(message);
    }
}
