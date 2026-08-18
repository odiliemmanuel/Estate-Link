package com.estatelink.notification.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InspectionRequestedEvent {

    private UUID requestId;
    private UUID slotId;
    private UUID listingId;
    private UUID agentId;
    private UUID applicantId;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private String message;
    private LocalDateTime requestedAt;
}
