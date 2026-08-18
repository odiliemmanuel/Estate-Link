package com.estatelink.user.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String type;        // always "Bearer"
    private UserResponse user;
}