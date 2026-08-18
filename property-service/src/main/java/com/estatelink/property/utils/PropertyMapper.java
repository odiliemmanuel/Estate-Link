package com.estatelink.property.utils;

import com.estatelink.property.domain.Property;
import com.estatelink.property.dto.responses.PropertyResponse;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {

    public PropertyResponse toResponse(Property property) {
        return PropertyResponse.builder()
                .id(property.getId())
                .ownerId(property.getOwnerId())
                .agentId(property.getAgentId())
                .title(property.getTitle())
                .description(property.getDescription())
                .address(property.getAddress())
                .city(property.getCity())
                .state(property.getState())
                .formattedAddress(property.getFormattedAddress())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .addressVerified(property.getAddressVerified())
                .propertyType(property.getPropertyType())
                .availabilityStatus(property.getAvailabilityStatus())
                .price(property.getPrice())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .squareFootage(property.getSquareFootage())
                .imageUrls(property.getImageUrls())
                .createdAt(property.getCreatedAt())
                .build();
    }
}