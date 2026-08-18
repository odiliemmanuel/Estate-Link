package com.estatelink.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferAcceptedEvent {

    private UUID offerId;
    private UUID listingId;
    private UUID agentId;
    private UUID applicantId;
    private BigDecimal amount;
    private String type;
    private LocalDateTime acceptedAt;
}
