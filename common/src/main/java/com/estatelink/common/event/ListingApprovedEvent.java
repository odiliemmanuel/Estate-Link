package com.estatelink.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingApprovedEvent {

    private UUID listingId;
    private UUID propertyId;
    private UUID ownerId;
    private UUID agentId;
    private String listingTitle;
    private String ownerEmail;
    private LocalDateTime approvedAt;
}
