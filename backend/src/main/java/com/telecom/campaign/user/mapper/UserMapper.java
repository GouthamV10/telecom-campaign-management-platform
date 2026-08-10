package com.telecom.campaign.user.mapper;

import com.telecom.campaign.user.dto.UserResponse;
import com.telecom.campaign.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user){
        return UserResponse.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail()).role(user.getRole()).enabled(user.getEnabled()).createdAt(user.getCreatedAt()).updatedAt(user.getUpdatedAt()).build();
    }
}
