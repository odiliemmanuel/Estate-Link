package com.estatelink.inspection.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published to the "inspection.requested" topic whenever an applicant
 * requests an inspection of a listing at one of an agent's slots.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
