package com.estatelink.property.utils;

import com.estatelink.property.domain.Listing;
import com.estatelink.property.dto.responses.ListingResponse;
import org.springframework.stereotype.Component;

@Component
public class ListingMapper {

    public ListingResponse toResponse(Listing listing) {
        return ListingResponse.builder()
                .id(listing.getId())
                .propertyId(listing.getPropertyId())
                .ownerId(listing.getOwnerId())
                .agentId(listing.getAgentId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .price(listing.getPrice())
                .purpose(listing.getPurpose())
                .status(listing.getStatus())
                .approved(listing.isApproved())
                .approvedAt(listing.getApprovedAt())
                .createdAt(listing.getCreatedAt())
                .build();
    }
}