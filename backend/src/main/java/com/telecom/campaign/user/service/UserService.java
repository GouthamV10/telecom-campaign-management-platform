package com.telecom.campaign.user.service;

import com.telecom.campaign.user.dto.RegisterRequest;
import com.telecom.campaign.user.dto.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);
}
