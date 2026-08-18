package com.estatelink.property.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ListingApprovedEvent {

    private UUID listingId;
    private UUID propertyId;
    private UUID ownerId;
    private UUID agentId;
    private String listingTitle;
    private String ownerEmail;
    private LocalDateTime approvedAt;
}
