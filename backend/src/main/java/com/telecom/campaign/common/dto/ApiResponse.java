package com.telecom.campaign.common.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private Boolean success;

    private Integer statusCode;

    private String message;

    private T data;
}
