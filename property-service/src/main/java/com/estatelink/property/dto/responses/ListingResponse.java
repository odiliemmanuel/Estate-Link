package com.estatelink.property.dto.responses;

import com.estatelink.property.domain.ListingStatus;
import com.estatelink.property.domain.Purpose;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ListingResponse {

    private UUID id;
    private UUID propertyId;
    private UUID ownerId;
    private UUID agentId;
    private String title;
    private String description;
    private BigDecimal price;
    private Purpose purpose;
    private ListingStatus status;
    private boolean approved;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
