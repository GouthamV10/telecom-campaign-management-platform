package com.telecom.campaign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class CampaignApplication {

	public static void main(String[] args) {
		log.info("Starting CampaignApplication");
		SpringApplication.run(CampaignApplication.class, args);
	}

}
