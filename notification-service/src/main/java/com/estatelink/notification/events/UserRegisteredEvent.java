package com.estatelink.notification.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private UUID userId;
    private String name;
    private String email;
    private String verificationToken;
    private LocalDateTime registeredAt;


}
