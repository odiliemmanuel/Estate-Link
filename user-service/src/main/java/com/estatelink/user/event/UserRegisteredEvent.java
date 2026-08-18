package com.estatelink.user.event;


import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class UserRegisteredEvent {

    private UUID userId;
    private String name;
    private String email;
    private String verificationToken;
    private LocalDateTime registeredAt;

    public UserRegisteredEvent() {}

    public UserRegisteredEvent(UUID userId, String name, String email, String verificationToken, LocalDateTime registeredAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.verificationToken = verificationToken;
        this.registeredAt = registeredAt;
    }


}
