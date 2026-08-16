package com.telecom.campaign.user.dto;

import io.micrometer.core.instrument.binder.BaseUnits;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
}
