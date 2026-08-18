package com.estatelink.user.utils;

import com.estatelink.user.data.model.User;
import com.estatelink.user.dtos.responses.UserResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt( LocalDateTime.now())
                .build();
    }


}
