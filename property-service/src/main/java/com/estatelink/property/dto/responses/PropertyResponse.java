package com.estatelink.property.dto.responses;

import com.estatelink.property.domain.AvailabilityStatus;
import com.estatelink.property.domain.PropertyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PropertyResponse {

    private UUID id;
    private UUID ownerId;
    private UUID agentId;
    private String title;
    private String description;
    private String address;
    private String city;
    private String state;
    private String formattedAddress;
    private Double latitude;
    private Double longitude;
    private Boolean addressVerified;
    private PropertyType propertyType;
    private AvailabilityStatus availabilityStatus;
    private BigDecimal price;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer squareFootage;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}